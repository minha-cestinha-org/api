package minhacestinha.api.service.gasto;

import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.dto.response.InflacaoResponse;
import minhacestinha.api.persistence.entity.ItemNota;
import minhacestinha.api.persistence.entity.Nota;
import minhacestinha.api.persistence.entity.Produto;
import minhacestinha.api.persistence.entity.ProdutoUsuario;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.ItemNotaRepository;
import minhacestinha.api.persistence.repository.NotaRepository;
import minhacestinha.api.persistence.repository.ProdutoUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GastoServiceUnitTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private ItemNotaRepository itemNotaRepository;

    @Mock
    private ProdutoUsuarioRepository produtoUsuarioRepository;

    @Mock
    private IpcaClient ipcaClient;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-29T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private GastoServiceImpl gastoService;

    @BeforeEach
    void setUp() {
        gastoService = new GastoServiceImpl(notaRepository, itemNotaRepository, produtoUsuarioRepository, ipcaClient, clock);
    }

    @Test
    void somaPorMesEPreencheMesesSemCompra() {
        User usuario = User.builder().id(1L).build();
        when(notaRepository.findByUsuarioIdAndAtivoTrueAndDataEmissaoGreaterThanEqual(1L, LocalDateTime.of(2026, 7, 1, 0, 0)))
                .thenReturn(List.of(
                        nota("100.00", LocalDateTime.of(2026, 7, 10, 9, 0)),
                        nota("214.87", LocalDateTime.of(2026, 9, 26, 10, 0)),
                        nota("50.13", LocalDateTime.of(2026, 9, 12, 18, 0))));

        List<GastoMensalResponse> gastos = gastoService.porMes(usuario, 3);

        assertThat(gastos).extracting(GastoMensalResponse::mes).containsExactly("2026-07", "2026-08", "2026-09");
        assertThat(gastos.get(0).total()).isEqualByComparingTo("100.00");
        assertThat(gastos.get(1).total()).isEqualByComparingTo("0");
        assertThat(gastos.get(1).compras()).isZero();
        assertThat(gastos.get(2).total()).isEqualByComparingTo("265.00");
        assertThat(gastos.get(2).compras()).isEqualTo(2);
    }

    @Test
    void inflacaoPonderaPeloGastoEUsaNomeDoUsuario() {
        User usuario = User.builder().id(1L).build();
        Produto leite = Produto.builder().id(10L).nome("leite integral itambé 1l").build();
        Produto arroz = Produto.builder().id(20L).nome("arroz tio joão 5kg").build();
        Produto pao = Produto.builder().id(30L).nome("pão francês").build();
        LocalDateTime outubro = LocalDateTime.of(2025, 10, 5, 10, 0);
        LocalDateTime setembro = LocalDateTime.of(2026, 9, 20, 10, 0);
        when(itemNotaRepository.findDoUsuarioDesde(1L, LocalDateTime.of(2025, 10, 1, 0, 0))).thenReturn(List.of(
                item(leite, "5.00", "4", outubro),
                item(arroz, "30.00", "1", outubro),
                item(pao, "1.00", "5", outubro),
                item(pao, "1.20", "5", outubro.plusDays(10)),
                item(leite, "6.00", "4", setembro),
                item(arroz, "27.00", "1", setembro)));
        when(produtoUsuarioRepository.findByUsuarioId(1L))
                .thenReturn(List.of(ProdutoUsuario.builder().produto(leite).nome("leite do joão").build()));
        when(ipcaClient.acumulado(YearMonth.of(2025, 10), YearMonth.of(2026, 9))).thenReturn(Optional.of(new BigDecimal("4.10")));

        InflacaoResponse inflacao = gastoService.inflacao(usuario, 12);

        // leite +20% com peso 20, arroz -10% com peso 30: (20*1.2 + 30*0.9) / 50 = 1.02. O pão só tem um mês.
        assertThat(inflacao.mesInicio()).isEqualTo("2025-10");
        assertThat(inflacao.mesFim()).isEqualTo("2026-09");
        assertThat(inflacao.variacaoCarrinho()).isEqualByComparingTo("2.00");
        assertThat(inflacao.ipca()).isEqualByComparingTo("4.10");
        assertThat(inflacao.produtosComparados()).isEqualTo(2);
        assertThat(inflacao.maioresAltas()).hasSize(1);
        assertThat(inflacao.maioresAltas().getFirst().nome()).isEqualTo("leite do joão");
        assertThat(inflacao.maioresAltas().getFirst().variacaoPercentual()).isEqualByComparingTo("20.00");
    }

    @Test
    void inflacaoSemComparacaoNemIpca() {
        User usuario = User.builder().id(1L).build();
        when(itemNotaRepository.findDoUsuarioDesde(1L, LocalDateTime.of(2026, 4, 1, 0, 0))).thenReturn(List.of());
        when(produtoUsuarioRepository.findByUsuarioId(1L)).thenReturn(List.of());
        when(ipcaClient.acumulado(YearMonth.of(2026, 4), YearMonth.of(2026, 9))).thenReturn(Optional.empty());

        InflacaoResponse inflacao = gastoService.inflacao(usuario, 6);

        assertThat(inflacao.variacaoCarrinho()).isNull();
        assertThat(inflacao.ipca()).isNull();
        assertThat(inflacao.produtosComparados()).isZero();
        assertThat(inflacao.maioresAltas()).isEmpty();
    }

    private static ItemNota item(Produto produto, String precoUnitario, String quantidade, LocalDateTime data) {
        BigDecimal preco = new BigDecimal(precoUnitario);
        BigDecimal qtd = new BigDecimal(quantidade);
        return ItemNota.builder().produto(produto).nota(Nota.builder().dataEmissao(data).build())
                .precoUnitario(preco).quantidade(qtd).precoTotal(preco.multiply(qtd)).build();
    }

    private static Nota nota(String valorPago, LocalDateTime data) {
        return Nota.builder().valorPago(new BigDecimal(valorPago)).dataEmissao(data).build();
    }
}
