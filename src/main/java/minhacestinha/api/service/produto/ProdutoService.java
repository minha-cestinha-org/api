package minhacestinha.api.service.produto;

import minhacestinha.api.dto.request.ProdutoPutRequest;
import minhacestinha.api.dto.response.ProdutoHistoricoResponse;
import minhacestinha.api.dto.response.ProdutoResumoResponse;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.entity.Produto;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import minhacestinha.api.service.padronizacao.ProdutoPadronizado;

import java.util.List;

public interface ProdutoService {

    /**
     * Encontra (ou cria) o produto de um item da nota, usando o cache "descrição + mercado" e depois o EAN.
     * Ao criar, usa a sugestão de padronização, se houver.
     */
    Produto resolver(ItemLido item, Mercado mercado, ProdutoPadronizado sugestao);

    List<ProdutoResumoResponse> listar(User usuario);

    ProdutoHistoricoResponse historico(Long produtoId, User usuario);

    /** Modo mercado: histórico do usuário para o produto do código de barras lido na gôndola. */
    ProdutoHistoricoResponse historicoPorEan(String ean, User usuario);

    /** Corrige nome, marca e categoria só para o usuário (não mexe no catálogo global). */
    void atualizar(Long produtoId, ProdutoPutRequest dto, User usuario);
}
