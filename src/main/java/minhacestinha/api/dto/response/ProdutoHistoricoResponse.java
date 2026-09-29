package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Produto com o resumo e todas as compras")
public record ProdutoHistoricoResponse(
        ProdutoResumoResponse resumo,
        List<PrecoHistoricoResponse> historico
) {
}
