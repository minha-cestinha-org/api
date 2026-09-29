package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Total gasto em um mês")
public record GastoMensalResponse(
        @Schema(example = "2026-09")
        String mes,
        BigDecimal total,
        int compras
) {
}
