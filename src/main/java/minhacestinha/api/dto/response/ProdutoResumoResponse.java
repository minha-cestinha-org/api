package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Produto que o usuário já comprou, com o comparativo de preços (card de preços)")
public record ProdutoResumoResponse(
        Long id,
        String nome,
        String marca,
        String categoria,
        @Schema(description = "Preço unitário na compra mais recente")
        BigDecimal ultimoPreco,
        LocalDateTime ultimaCompra,
        String ultimoMercado,
        @Schema(description = "Preço unitário na compra anterior à mais recente")
        BigDecimal precoAnterior,
        @Schema(description = "Variação percentual entre o preço anterior e o último", example = "21.73")
        BigDecimal variacaoPercentual,
        BigDecimal menorPreco,
        String mercadoMenorPreco,
        @Schema(description = "Média dos preços unitários pagos")
        BigDecimal precoMedio,
        int vezesComprado
) {
}
