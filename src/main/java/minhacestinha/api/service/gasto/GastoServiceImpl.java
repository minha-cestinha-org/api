package minhacestinha.api.service.gasto;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.dto.response.InflacaoResponse;
import minhacestinha.api.dto.response.InflacaoResponse.VariacaoProdutoResponse;
import minhacestinha.api.persistence.entity.ItemNota;
import minhacestinha.api.persistence.entity.Nota;
import minhacestinha.api.persistence.entity.Produto;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.ItemNotaRepository;
import minhacestinha.api.persistence.repository.NotaRepository;
import minhacestinha.api.persistence.repository.ProdutoUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GastoServiceImpl implements GastoService {

    private static final int MAIORES_ALTAS = 3;

    private final NotaRepository notaRepository;
    private final ItemNotaRepository itemNotaRepository;
    private final ProdutoUsuarioRepository produtoUsuarioRepository;
    private final IpcaClient ipcaClient;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<GastoMensalResponse> porMes(User usuario, int meses) {
        YearMonth atual = YearMonth.now(clock);
        YearMonth primeiro = atual.minusMonths(meses - 1L);

        Map<YearMonth, List<Nota>> notasPorMes = notaRepository
                .findByUsuarioIdAndAtivoTrueAndDataEmissaoGreaterThanEqual(usuario.getId(), primeiro.atDay(1).atStartOfDay())
                .stream()
                .collect(Collectors.groupingBy(nota -> YearMonth.from(nota.getDataEmissao())));

        return IntStream.range(0, meses)
                .mapToObj(primeiro::plusMonths)
                .map(mes -> {
                    List<Nota> notas = notasPorMes.getOrDefault(mes, List.of());
                    BigDecimal total = notas.stream().map(Nota::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new GastoMensalResponse(mes.toString(), total, notas.size());
                })
                .toList();
    }

    /**
     * Compara o primeiro e o último preço de cada produto comprado em pelo menos dois meses diferentes.
     * O peso de cada produto é o quanto foi gasto nele na primeira compra. Sem transação: o IPCA vem do Banco Central.
     */
    @Override
    public InflacaoResponse inflacao(User usuario, int meses) {
        YearMonth fim = YearMonth.now(clock);
        YearMonth inicio = fim.minusMonths(meses - 1L);

        Map<Produto, List<ItemNota>> comprasPorProduto = itemNotaRepository
                .findDoUsuarioDesde(usuario.getId(), inicio.atDay(1).atStartOfDay())
                .stream()
                .collect(Collectors.groupingBy(ItemNota::getProduto, LinkedHashMap::new, Collectors.toList()));

        Map<Long, String> nomes = produtoUsuarioRepository.findByUsuarioId(usuario.getId()).stream()
                .collect(Collectors.toMap(correcao -> correcao.getProduto().getId(), correcao -> correcao.getNome()));

        BigDecimal pesoTotal = BigDecimal.ZERO;
        BigDecimal pesoCorrigido = BigDecimal.ZERO;
        List<VariacaoProdutoResponse> variacoes = new ArrayList<>();

        for (Map.Entry<Produto, List<ItemNota>> entry : comprasPorProduto.entrySet()) {
            List<ItemNota> compras = entry.getValue();
            ItemNota primeira = compras.getFirst();
            ItemNota ultima = compras.getLast();
            if (YearMonth.from(primeira.getNota().getDataEmissao()).equals(YearMonth.from(ultima.getNota().getDataEmissao()))
                    || primeira.getPrecoUnitario().signum() <= 0) {
                continue;
            }
            BigDecimal razao = ultima.getPrecoUnitario().divide(primeira.getPrecoUnitario(), 6, RoundingMode.HALF_UP);
            BigDecimal peso = primeira.getPrecoTotal();
            pesoTotal = pesoTotal.add(peso);
            pesoCorrigido = pesoCorrigido.add(peso.multiply(razao));

            Produto produto = entry.getKey();
            variacoes.add(new VariacaoProdutoResponse(produto.getId(), nomes.getOrDefault(produto.getId(), produto.getNome()),
                    primeira.getPrecoUnitario(), ultima.getPrecoUnitario(), percentual(razao)));
        }

        BigDecimal variacaoCarrinho = pesoTotal.signum() > 0
                ? percentual(pesoCorrigido.divide(pesoTotal, 6, RoundingMode.HALF_UP))
                : null;
        List<VariacaoProdutoResponse> maioresAltas = variacoes.stream()
                .filter(variacao -> variacao.variacaoPercentual().signum() > 0)
                .sorted(Comparator.comparing(VariacaoProdutoResponse::variacaoPercentual).reversed())
                .limit(MAIORES_ALTAS)
                .toList();
        BigDecimal ipca = ipcaClient.acumulado(inicio, fim).orElse(null);

        return new InflacaoResponse(inicio.toString(), fim.toString(), variacaoCarrinho, ipca, variacoes.size(), maioresAltas);
    }

    private static BigDecimal percentual(BigDecimal razao) {
        return razao.subtract(BigDecimal.ONE).movePointRight(2).setScale(2, RoundingMode.HALF_UP);
    }
}
