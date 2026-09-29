package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Um ponto do histórico de preço de um produto")
public record PrecoHistoricoResponse(
        Long notaId,
        LocalDateTime data,
        String mercado,
        BigDecimal precoUnitario,
        BigDecimal quantidade,
        String unidade
) {
}
