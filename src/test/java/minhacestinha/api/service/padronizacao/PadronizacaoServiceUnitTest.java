package minhacestinha.api.service.padronizacao;

import minhacestinha.api.persistence.entity.MapeamentoDescricao;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.entity.Produto;
import minhacestinha.api.persistence.repository.MapeamentoDescricaoRepository;
import minhacestinha.api.persistence.repository.MercadoRepository;
import minhacestinha.api.persistence.repository.ProdutoRepository;
import minhacestinha.api.service.nfce.NotaLida;
import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import minhacestinha.api.service.nfce.NotaLida.MercadoLido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PadronizacaoServiceUnitTest {

    @Mock
    private MercadoRepository mercadoRepository;

    @Mock
    private MapeamentoDescricaoRepository mapeamentoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CosmosClient cosmosClient;

    @Mock
    private ClaudePadronizador claudePadronizador;

    @InjectMocks
    private PadronizacaoServiceImpl service;

    @Test
    void soPadronizaItensNovosUsandoCosmosAntesDaIa() {
        when(mercadoRepository.findByCnpj("12345678000190")).thenReturn(Optional.of(Mercado.builder().id(1L).build()));
        when(mapeamentoRepository.findByDescricaoBrutaAndMercadoId(any(), eq(1L))).thenReturn(Optional.empty());
        when(mapeamentoRepository.findByDescricaoBrutaAndMercadoId("BAN PRATA KG", 1L))
                .thenReturn(Optional.of(MapeamentoDescricao.builder().build()));
        when(produtoRepository.findByEan("7891000100103")).thenReturn(Optional.of(Produto.builder().id(9L).build()));
        when(produtoRepository.findByEan("7896051111115")).thenReturn(Optional.empty());
        when(cosmosClient.buscarPorEan("7896051111115"))
                .thenReturn(Optional.of(new ProdutoPadronizado("leite integral itambé 1l", "itambé", null)));
        when(cosmosClient.buscarPorEan(null)).thenReturn(Optional.empty());
        when(claudePadronizador.padronizar(List.of("DET LIQ YPE NEUT 500"), "MERCADO DA VILA"))
                .thenReturn(Map.of("DET LIQ YPE NEUT 500", new ProdutoPadronizado("detergente ypê neutro 500ml", "ypê", "limpeza")));

        Map<String, ProdutoPadronizado> resultado = service.padronizarNovos(nota(
                item("LTE COND INTEG ITAMB 1L", "7896051111115"),
                item("BAN PRATA KG", null),
                item("CAFE TORR PILAO 500G", "7891000100103"),
                item("DET LIQ YPE NEUT 500", null)));

        assertThat(resultado).containsOnlyKeys("LTE COND INTEG ITAMB 1L", "DET LIQ YPE NEUT 500");
        assertThat(resultado.get("DET LIQ YPE NEUT 500").categoria()).isEqualTo("limpeza");
        verify(claudePadronizador).padronizar(List.of("DET LIQ YPE NEUT 500"), "MERCADO DA VILA");
    }

    private static NotaLida nota(ItemLido... itens) {
        return new NotaLida("chave", "SP", new MercadoLido("12345678000190", "MERCADO DA VILA", null),
                LocalDateTime.now(), BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, List.of(itens), List.of());
    }

    private static ItemLido item(String descricao, String ean) {
        return new ItemLido(1, descricao, ean, ean, BigDecimal.ONE, "UN", BigDecimal.ONE, BigDecimal.ONE);
    }
}
