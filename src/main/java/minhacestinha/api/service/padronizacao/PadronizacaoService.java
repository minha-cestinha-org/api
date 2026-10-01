package minhacestinha.api.service.padronizacao;

import minhacestinha.api.service.nfce.NotaLida;

import java.util.Map;

public interface PadronizacaoService {

    /**
     * Padroniza os itens da nota que ainda não estão no catálogo (cache "descrição + mercado" ou EAN conhecido).
     * Faz chamadas externas (Cosmos e Claude): chame fora de transação.
     *
     * @return padronização indexada pela descrição bruta; itens sem sugestão ficam de fora
     */
    Map<String, ProdutoPadronizado> padronizarNovos(NotaLida nota);
}
