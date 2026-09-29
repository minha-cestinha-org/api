package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item de uma nota")
public record ItemNotaResponse(
        Integer numero,
        Long produtoId,
        @Schema(description = "Nome padronizado do produto", example = "leite integral itambé 1L")
        String produtoNome,
        @Schema(description = "Descrição como veio na nota", example = "LTE COND INTEG ITAMB 1L")
        String descricaoBruta,
        BigDecimal quantidade,
        String unidade,
        BigDecimal precoUnitario,
        BigDecimal precoTotal
) {
}
