package minhacestinha.api.service.produto;

import minhacestinha.api.dto.response.ProdutoResumoResponse;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.ItemNota;
import minhacestinha.api.persistence.entity.MapeamentoDescricao;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.entity.Nota;
import minhacestinha.api.persistence.entity.Produto;
import minhacestinha.api.persistence.entity.ProdutoUsuario;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.ItemNotaRepository;
import minhacestinha.api.persistence.repository.MapeamentoDescricaoRepository;
import minhacestinha.api.persistence.repository.ProdutoRepository;
import minhacestinha.api.persistence.repository.ProdutoUsuarioRepository;
import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import minhacestinha.api.service.padronizacao.ProdutoPadronizado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceUnitTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MapeamentoDescricaoRepository mapeamentoRepository;

    @Mock
    private ItemNotaRepository itemNotaRepository;

    @Mock
    private ProdutoUsuarioRepository produtoUsuarioRepository;

    @Mock
    private Mensagens mensagens;

    @InjectMocks
    private ProdutoServiceImpl service;

    private final Mercado mercado = Mercado.builder().id(1L).cnpj("12345678000190").nome("mercado da vila").build();

    @Test
    void usaOCacheQuandoADescricaoJaFoiVistaNoMercado() {
        Produto leite = Produto.builder().id(10L).nome("leite integral itambé 1L").build();
        when(mapeamentoRepository.findByDescricaoBrutaAndMercadoId("LTE COND INTEG ITAMB 1L", 1L))
                .thenReturn(Optional.of(MapeamentoDescricao.builder().produto(leite).build()));

        Produto produto = service.resolver(item("LTE COND INTEG ITAMB 1L", "7896051111115"), mercado, null);

        assertThat(produto).isSameAs(leite);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void reaproveitaProdutoPeloEanQuandoADescricaoEhNova() {
        Produto leite = Produto.builder().id(10L).ean("7896051111115").nome("leite integral itambé 1L").build();
        when(mapeamentoRepository.findByDescricaoBrutaAndMercadoId(any(), any())).thenReturn(Optional.empty());
        when(produtoRepository.findByEan("7896051111115")).thenReturn(Optional.of(leite));

        Produto produto = service.resolver(item("LEITE INT ITAMBE 1LT", "7896051111115"), mercado, null);

        assertThat(produto).isSameAs(leite);
        verify(produtoRepository, never()).save(any());
        verify(mapeamentoRepository).save(any());
    }

    @Test
    void criaProdutoComNomeEmMinusculoQuandoNaoTemEan() {
        when(mapeamentoRepository.findByDescricaoBrutaAndMercadoId(any(), any())).thenReturn(Optional.empty());
        when(produtoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Produto produto = service.resolver(item("BAN  PRATA KG", null), mercado, null);

        assertThat(produto.getNome()).isEqualTo("ban prata kg");
        assertThat(produto.getEan()).isNull();
    }

    @Test
    void usaASugestaoDePadronizacaoAoCriarProduto() {
        when(mapeamentoRepository.findByDescricaoBrutaAndMercadoId(any(), any())).thenReturn(Optional.empty());
        when(produtoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Produto produto = service.resolver(item("LTE COND INTEG ITAMB 1L", null), mercado,
                new ProdutoPadronizado("leite integral itambé 1L", "itambé", "frios e laticínios"));

        assertThat(produto.getNome()).isEqualTo("leite integral itambé 1L");
        assertThat(produto.getMarca()).isEqualTo("itambé");
        assertThat(produto.getCategoria()).isEqualTo("frios e laticínios");
    }

    @Test
    void resumeComparativoDePrecos() {
        User usuario = User.builder().id(1L).build();
        Produto leite = Produto.builder().id(10L).nome("leite integral itambé 1L").build();
        Mercado economico = Mercado.builder().id(2L).nome("super econômico").build();
        when(itemNotaRepository.findDoUsuario(1L)).thenReturn(List.of(
                compra(leite, economico, "5.99", LocalDateTime.of(2026, 7, 4, 10, 0)),
                compra(leite, mercado, "6.49", LocalDateTime.of(2026, 8, 22, 10, 0)),
                compra(leite, mercado, "7.90", LocalDateTime.of(2026, 9, 26, 10, 0))));

        ProdutoResumoResponse resumo = service.listar(usuario).getFirst();

        assertThat(resumo.ultimoPreco()).isEqualByComparingTo("7.90");
        assertThat(resumo.precoAnterior()).isEqualByComparingTo("6.49");
        assertThat(resumo.variacaoPercentual()).isEqualByComparingTo("21.73");
        assertThat(resumo.menorPreco()).isEqualByComparingTo("5.99");
        assertThat(resumo.mercadoMenorPreco()).isEqualTo("super econômico");
        assertThat(resumo.precoMedio()).isEqualByComparingTo("6.79");
        assertThat(resumo.vezesComprado()).isEqualTo(3);
    }

    @Test
    void correcaoDoUsuarioSobrepoeONomeSemMexerNoCatalogo() {
        User usuario = User.builder().id(1L).build();
        Produto leite = Produto.builder().id(10L).nome("lte cond integ itamb 1l").build();
        when(itemNotaRepository.findDoUsuario(1L)).thenReturn(List.of(
                compra(leite, mercado, "7.90", LocalDateTime.of(2026, 9, 26, 10, 0))));
        when(produtoUsuarioRepository.findByUsuarioId(1L)).thenReturn(List.of(ProdutoUsuario.builder()
                .produto(leite).nome("leite integral itambé 1L").marca("itambé").build()));

        ProdutoResumoResponse resumo = service.listar(usuario).getFirst();

        assertThat(resumo.nome()).isEqualTo("leite integral itambé 1L");
        assertThat(resumo.marca()).isEqualTo("itambé");
        assertThat(leite.getNome()).isEqualTo("lte cond integ itamb 1l");
    }

    private static ItemLido item(String descricao, String ean) {
        return new ItemLido(1, descricao, ean, ean, BigDecimal.ONE, "UN", BigDecimal.TEN, BigDecimal.TEN);
    }

    private static ItemNota compra(Produto produto, Mercado mercado, String preco, LocalDateTime data) {
        Nota nota = Nota.builder().id(data.getDayOfYear() * 1L).mercado(mercado).dataEmissao(data).build();
        return ItemNota.builder().nota(nota).produto(produto).precoUnitario(new BigDecimal(preco))
                .quantidade(BigDecimal.ONE).unidade("UN").build();
    }
}
