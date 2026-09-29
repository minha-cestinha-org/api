package minhacestinha.api.service.produto;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.dto.request.ProdutoPutRequest;
import minhacestinha.api.dto.response.PrecoHistoricoResponse;
import minhacestinha.api.dto.response.ProdutoHistoricoResponse;
import minhacestinha.api.dto.response.ProdutoResumoResponse;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.ItemNota;
import minhacestinha.api.persistence.entity.MapeamentoDescricao;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.entity.Produto;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.ItemNotaRepository;
import minhacestinha.api.persistence.repository.MapeamentoDescricaoRepository;
import minhacestinha.api.persistence.repository.ProdutoRepository;
import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MapeamentoDescricaoRepository mapeamentoRepository;
    private final ItemNotaRepository itemNotaRepository;
    private final Mensagens mensagens;

    @Override
    @Transactional
    public Produto resolver(ItemLido item, Mercado mercado) {
        return mapeamentoRepository.findByDescricaoBrutaAndMercadoId(item.descricaoBruta(), mercado.getId())
                .map(MapeamentoDescricao::getProduto)
                .orElseGet(() -> criarMapeamento(item, mercado));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResumoResponse> listar(User usuario) {
        Map<Produto, List<ItemNota>> comprasPorProduto = itemNotaRepository.findDoUsuario(usuario.getId())
                .stream()
                .collect(Collectors.groupingBy(ItemNota::getProduto, LinkedHashMap::new, Collectors.toList()));

        return comprasPorProduto.entrySet()
                .stream()
                .map(entry -> resumir(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ProdutoResumoResponse::ultimaCompra).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoHistoricoResponse historico(Long produtoId, User usuario) {
        Produto produto = buscarProduto(produtoId);
        List<ItemNota> compras = itemNotaRepository.findDoUsuarioPorProduto(usuario.getId(), produtoId);
        if (compras.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, mensagens.get("produto.nao.encontrado"));
        }

        List<PrecoHistoricoResponse> historico = compras.stream()
                .map(item -> new PrecoHistoricoResponse(
                        item.getNota().getId(),
                        item.getNota().getDataEmissao(),
                        item.getNota().getMercado().getNome(),
                        item.getPrecoUnitario(),
                        item.getQuantidade(),
                        item.getUnidade()))
                .toList();

        return new ProdutoHistoricoResponse(resumir(produto, compras), historico);
    }

    @Override
    @Transactional
    public void atualizar(Long produtoId, ProdutoPutRequest dto, User usuario) {
        if (itemNotaRepository.findDoUsuarioPorProduto(usuario.getId(), produtoId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, mensagens.get("produto.nao.encontrado"));
        }
        Produto produto = buscarProduto(produtoId);
        produto.setNome(dto.nome().trim());
        produto.setMarca(dto.marca());
        produto.setCategoria(dto.categoria());
        produtoRepository.save(produto);
    }

    private Produto criarMapeamento(ItemLido item, Mercado mercado) {
        Produto produto = Optional.ofNullable(item.ean())
                .flatMap(produtoRepository::findByEan)
                .orElseGet(() -> produtoRepository.save(Produto.builder()
                        .ean(item.ean())
                        .nome(nomeInicial(item.descricaoBruta()))
                        .build()));

        mapeamentoRepository.save(MapeamentoDescricao.builder()
                .descricaoBruta(item.descricaoBruta())
                .mercado(mercado)
                .produto(produto)
                .build());

        return produto;
    }

    /** Enquanto a padronização com IA não entra, o nome é a descrição da nota em minúsculo. */
    private static String nomeInicial(String descricaoBruta) {
        return descricaoBruta.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Monta o comparativo a partir das compras, que vêm ordenadas da mais antiga para a mais recente. */
    private ProdutoResumoResponse resumir(Produto produto, List<ItemNota> compras) {
        ItemNota ultima = compras.getLast();
        ItemNota menor = compras.stream().min(Comparator.comparing(ItemNota::getPrecoUnitario)).orElse(ultima);
        BigDecimal precoAnterior = compras.size() > 1 ? compras.get(compras.size() - 2).getPrecoUnitario() : null;
        BigDecimal soma = compras.stream().map(ItemNota::getPrecoUnitario).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProdutoResumoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getMarca(),
                produto.getCategoria(),
                ultima.getPrecoUnitario(),
                ultima.getNota().getDataEmissao(),
                ultima.getNota().getMercado().getNome(),
                precoAnterior,
                variacaoPercentual(precoAnterior, ultima.getPrecoUnitario()),
                menor.getPrecoUnitario(),
                menor.getNota().getMercado().getNome(),
                soma.divide(BigDecimal.valueOf(compras.size()), 2, RoundingMode.HALF_UP),
                compras.size());
    }

    private static BigDecimal variacaoPercentual(BigDecimal anterior, BigDecimal atual) {
        if (anterior == null || anterior.signum() == 0) {
            return null;
        }
        return atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 2, RoundingMode.HALF_UP);
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, mensagens.get("produto.nao.encontrado")));
    }
}
