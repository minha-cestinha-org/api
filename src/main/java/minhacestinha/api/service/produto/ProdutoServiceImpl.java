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
import minhacestinha.api.persistence.entity.ProdutoUsuario;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.ItemNotaRepository;
import minhacestinha.api.persistence.repository.MapeamentoDescricaoRepository;
import minhacestinha.api.persistence.repository.ProdutoRepository;
import minhacestinha.api.persistence.repository.ProdutoUsuarioRepository;
import minhacestinha.api.service.nfce.NumeroBr;
import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import minhacestinha.api.service.padronizacao.ProdutoPadronizado;
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
    private final ProdutoUsuarioRepository produtoUsuarioRepository;
    private final Mensagens mensagens;

    @Override
    @Transactional
    public Produto resolver(ItemLido item, Mercado mercado, ProdutoPadronizado sugestao) {
        return mapeamentoRepository.findByDescricaoBrutaAndMercadoId(item.descricaoBruta(), mercado.getId())
                .map(MapeamentoDescricao::getProduto)
                .orElseGet(() -> criarMapeamento(item, mercado, sugestao));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResumoResponse> listar(User usuario) {
        Map<Produto, List<ItemNota>> comprasPorProduto = itemNotaRepository.findDoUsuario(usuario.getId())
                .stream()
                .collect(Collectors.groupingBy(ItemNota::getProduto, LinkedHashMap::new, Collectors.toList()));
        Map<Long, ProdutoUsuario> correcoes = produtoUsuarioRepository.findByUsuarioId(usuario.getId())
                .stream()
                .collect(Collectors.toMap(correcao -> correcao.getProduto().getId(), correcao -> correcao));

        return comprasPorProduto.entrySet()
                .stream()
                .map(entry -> resumir(entry.getKey(), entry.getValue(), correcoes.get(entry.getKey().getId())))
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

        ProdutoUsuario correcao = produtoUsuarioRepository.findByUsuarioIdAndProdutoId(usuario.getId(), produtoId).orElse(null);
        return new ProdutoHistoricoResponse(resumir(produto, compras, correcao), historico);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoHistoricoResponse historicoPorEan(String ean, User usuario) {
        String codigo = ean == null ? "" : ean.replaceAll("\\D", "");
        if (!NumeroBr.gtinValido(codigo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagens.get("produto.ean.invalido"));
        }
        Produto produto = produtoRepository.findByEan(NumeroBr.normalizarGtin(codigo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, mensagens.get("produto.nunca.comprado")));
        if (itemNotaRepository.findDoUsuarioPorProduto(usuario.getId(), produto.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, mensagens.get("produto.nunca.comprado"));
        }
        return historico(produto.getId(), usuario);
    }

    @Override
    @Transactional
    public void atualizar(Long produtoId, ProdutoPutRequest dto, User usuario) {
        if (itemNotaRepository.findDoUsuarioPorProduto(usuario.getId(), produtoId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, mensagens.get("produto.nao.encontrado"));
        }
        ProdutoUsuario correcao = produtoUsuarioRepository.findByUsuarioIdAndProdutoId(usuario.getId(), produtoId)
                .orElseGet(() -> ProdutoUsuario.builder()
                        .usuario(usuario)
                        .produto(buscarProduto(produtoId))
                        .build());
        correcao.setNome(dto.nome().trim());
        correcao.setMarca(dto.marca());
        correcao.setCategoria(dto.categoria());
        produtoUsuarioRepository.save(correcao);
    }

    private Produto criarMapeamento(ItemLido item, Mercado mercado, ProdutoPadronizado sugestao) {
        Produto produto = Optional.ofNullable(item.ean())
                .flatMap(produtoRepository::findByEan)
                .orElseGet(() -> produtoRepository.save(novoProduto(item, sugestao)));

        mapeamentoRepository.save(MapeamentoDescricao.builder()
                .descricaoBruta(item.descricaoBruta())
                .mercado(mercado)
                .produto(produto)
                .build());

        return produto;
    }

    /** Sem sugestão (Cosmos e IA desligados ou sem resposta), o nome é a descrição da nota em minúsculo. */
    private static Produto novoProduto(ItemLido item, ProdutoPadronizado sugestao) {
        boolean temNome = sugestao != null && sugestao.nome() != null && !sugestao.nome().isBlank();
        return Produto.builder()
                .ean(item.ean())
                .nome(temNome ? limitar(sugestao.nome().trim(), 200) : nomeInicial(item.descricaoBruta()))
                .marca(sugestao == null ? null : limitar(sugestao.marca(), 100))
                .categoria(sugestao == null ? null : limitar(sugestao.categoria(), 50))
                .build();
    }

    private static String nomeInicial(String descricaoBruta) {
        return descricaoBruta.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String limitar(String texto, int tamanho) {
        return texto == null || texto.length() <= tamanho ? texto : texto.substring(0, tamanho);
    }

    /** Monta o comparativo a partir das compras (da mais antiga para a mais recente), com a correção do usuário, se houver. */
    private ProdutoResumoResponse resumir(Produto produto, List<ItemNota> compras, ProdutoUsuario correcao) {
        ItemNota ultima = compras.getLast();
        ItemNota menor = compras.stream().min(Comparator.comparing(ItemNota::getPrecoUnitario)).orElse(ultima);
        BigDecimal precoAnterior = compras.size() > 1 ? compras.get(compras.size() - 2).getPrecoUnitario() : null;
        BigDecimal soma = compras.stream().map(ItemNota::getPrecoUnitario).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProdutoResumoResponse(
                produto.getId(),
                correcao != null ? correcao.getNome() : produto.getNome(),
                correcao != null ? correcao.getMarca() : produto.getMarca(),
                correcao != null ? correcao.getCategoria() : produto.getCategoria(),
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
