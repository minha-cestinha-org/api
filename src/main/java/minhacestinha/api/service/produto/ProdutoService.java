package minhacestinha.api.service.produto;

import minhacestinha.api.dto.request.ProdutoPutRequest;
import minhacestinha.api.dto.response.ProdutoHistoricoResponse;
import minhacestinha.api.dto.response.ProdutoResumoResponse;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.entity.Produto;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.service.nfce.NotaLida.ItemLido;

import java.util.List;

public interface ProdutoService {

    /** Encontra (ou cria) o produto de um item da nota, usando o cache "descrição + mercado". */
    Produto resolver(ItemLido item, Mercado mercado);

    List<ProdutoResumoResponse> listar(User usuario);

    ProdutoHistoricoResponse historico(Long produtoId, User usuario);

    void atualizar(Long produtoId, ProdutoPutRequest dto, User usuario);
}
