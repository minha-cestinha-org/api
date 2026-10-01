package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Inflação pessoal: quanto subiram os produtos que o usuário compra, comparado ao IPCA")
public record InflacaoResponse(
        @Schema(example = "2025-10")
        String mesInicio,
        @Schema(example = "2026-09")
        String mesFim,
        @Schema(description = "Variação do carrinho em %, ponderada pelo gasto em cada produto, ou null se ainda não dá pra comparar", example = "11.20")
        BigDecimal variacaoCarrinho,
        @Schema(description = "IPCA acumulado no período em %, ou null se o Banco Central não respondeu", example = "4.10")
        BigDecimal ipca,
        @Schema(description = "Quantos produtos entraram na conta (comprados em pelo menos dois meses diferentes)")
        int produtosComparados,
        List<VariacaoProdutoResponse> maioresAltas
) {

    @Schema(description = "Variação de preço de um produto no período")
    public record VariacaoProdutoResponse(Long produtoId, String nome, BigDecimal precoInicial, BigDecimal precoFinal,
                                          BigDecimal variacaoPercentual) {
    }
}
