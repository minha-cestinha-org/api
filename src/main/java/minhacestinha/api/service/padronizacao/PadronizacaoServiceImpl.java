package minhacestinha.api.service.padronizacao;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.repository.MapeamentoDescricaoRepository;
import minhacestinha.api.persistence.repository.MercadoRepository;
import minhacestinha.api.persistence.repository.ProdutoRepository;
import minhacestinha.api.service.nfce.NotaLida;
import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PadronizacaoServiceImpl implements PadronizacaoService {

    private final MercadoRepository mercadoRepository;
    private final MapeamentoDescricaoRepository mapeamentoRepository;
    private final ProdutoRepository produtoRepository;
    private final CosmosClient cosmosClient;
    private final ClaudePadronizador claudePadronizador;

    @Override
    public Map<String, ProdutoPadronizado> padronizarNovos(NotaLida nota) {
        Optional<Long> mercadoId = mercadoRepository.findByCnpj(nota.mercado().cnpj()).map(Mercado::getId);
        List<ItemLido> novos = nota.itens().stream()
                .filter(item -> mercadoId.flatMap(id -> mapeamentoRepository.findByDescricaoBrutaAndMercadoId(item.descricaoBruta(), id)).isEmpty())
                .filter(item -> item.ean() == null || produtoRepository.findByEan(item.ean()).isEmpty())
                .toList();

        Map<String, ProdutoPadronizado> resultado = new HashMap<>();
        novos.forEach(item -> cosmosClient.buscarPorEan(item.ean())
                .ifPresent(produto -> resultado.put(item.descricaoBruta(), produto)));

        List<String> semSugestao = novos.stream()
                .map(ItemLido::descricaoBruta)
                .filter(descricao -> !resultado.containsKey(descricao))
                .distinct()
                .toList();
        resultado.putAll(claudePadronizador.padronizar(semSugestao, nota.mercado().nome()));

        return resultado;
    }
}
