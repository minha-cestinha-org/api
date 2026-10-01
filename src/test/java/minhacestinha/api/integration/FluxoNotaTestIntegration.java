package minhacestinha.api.integration;

import com.jayway.jsonpath.JsonPath;
import minhacestinha.api.service.gasto.IpcaClient;
import minhacestinha.api.service.nfce.Fixtures;
import minhacestinha.api.service.nfce.SefazClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Clock;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo com banco (H2 no modo PostgreSQL, migrations do Flyway) e a Sefaz simulada:
 * cadastro → login → importar nota → compras → preços → gastos → correção → remoção → exportar → apagar conta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoNotaTestIntegration {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SefazClient sefazClient;

    @MockitoBean
    private IpcaClient ipcaClient;

    @TestConfiguration
    static class RelogioFixo {

        @Bean
        @Primary
        Clock relogioDeTeste() {
            return Clock.fixed(Instant.parse("2026-09-29T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        }
    }

    @Test
    void importaNotaEMostraPrecosEGastos() throws Exception {
        when(sefazClient.buscar(anyString())).thenReturn(Fixtures.html("sp-nota-exemplo.html"));
        when(ipcaClient.acumulado(any(), any())).thenReturn(Optional.of(new BigDecimal("4.10")));

        mockMvc.perform(json(post("/api/auth/register"), """
                        {"nome": "Eduardo", "email": "Edu@Email.com", "senha": "senha-forte-123", "aceitouTermos": true}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("edu@email.com"));

        mockMvc.perform(json(post("/api/auth/register"), """
                        {"nome": "Outro", "email": "edu@email.com", "senha": "senha-forte-123", "aceitouTermos": true}
                        """))
                .andExpect(status().isConflict());

        String login = mockMvc.perform(json(post("/api/auth/login"), """
                        {"email": "edu@email.com", "senha": "senha-forte-123"}
                        """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = "Bearer " + JsonPath.read(login, "$.token");

        mockMvc.perform(get("/api/notas")).andExpect(status().isUnauthorized());

        String importada = mockMvc.perform(json(post("/api/notas/qrcode"), """
                        {"conteudo": "%s"}
                        """.formatted(Fixtures.QR_CODE_EXEMPLO)).header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mercado.nome").value("MERCADO DA VILA LTDA"))
                .andExpect(jsonPath("$.valorPago").value(50.00))
                .andExpect(jsonPath("$.itens", hasSize(4)))
                .andExpect(jsonPath("$.itens[0].produtoNome").value("lte cond integ itamb 1l"))
                .andReturn().getResponse().getContentAsString();
        Integer notaId = JsonPath.read(importada, "$.id");
        Integer produtoId = JsonPath.read(importada, "$.itens[0].produtoId");

        mockMvc.perform(json(post("/api/notas/qrcode"), """
                        {"conteudo": "%s"}
                        """.formatted(Fixtures.CHAVE_EXEMPLO)).header("Authorization", token))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/notas").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantidadeItens").value(4));

        mockMvc.perform(get("/api/produtos").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        mockMvc.perform(get("/api/gastos/mensal?meses=3").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].mes").value("2026-09"))
                .andExpect(jsonPath("$[2].total").value(50.00))
                .andExpect(jsonPath("$[2].compras").value(1));

        mockMvc.perform(get("/api/gastos/mensal?meses=0").header("Authorization", token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/gastos/inflacao").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mesInicio").value("2025-10"))
                .andExpect(jsonPath("$.ipca").value(4.10))
                .andExpect(jsonPath("$.produtosComparados").value(0));

        mockMvc.perform(json(put("/api/produtos/" + produtoId), """
                        {"nome": "leite integral itambé 1L", "marca": "itambé", "categoria": "frios e laticínios"}
                        """).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/produtos/" + produtoId + "/historico").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.nome").value("leite integral itambé 1L"))
                .andExpect(jsonPath("$.resumo.ultimoPreco").value(7.90))
                .andExpect(jsonPath("$.historico", hasSize(1)));

        mockMvc.perform(patch("/api/notas/" + notaId + "/desativar").header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notas").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/users/me/dados").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conta.email").value("edu@email.com"))
                .andExpect(jsonPath("$.termosAceitosEm").exists())
                .andExpect(jsonPath("$.notas", hasSize(1)))
                .andExpect(jsonPath("$.notas[0].itens", hasSize(4)));

        mockMvc.perform(delete("/api/users/me").header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me").header("Authorization", token))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(json(post("/api/auth/login"), """
                        {"email": "edu@email.com", "senha": "senha-forte-123"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recusaCadastroSemAceitarOsTermos() throws Exception {
        mockMvc.perform(json(post("/api/auth/register"), """
                        {"nome": "Sem termos", "email": "semtermos@email.com", "senha": "senha-forte-123", "aceitouTermos": false}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recusaLoginComSenhaErrada() throws Exception {
        mockMvc.perform(json(post("/api/auth/login"), """
                        {"email": "ninguem@email.com", "senha": "errada-123"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
