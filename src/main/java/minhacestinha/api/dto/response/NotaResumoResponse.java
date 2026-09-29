package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Resumo de uma compra, para a lista de compras")
public record NotaResumoResponse(
        Long id,
        String mercado,
        LocalDateTime dataEmissao,
        BigDecimal valorPago,
        int quantidadeItens
) {
}
