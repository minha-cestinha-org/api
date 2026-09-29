package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Nota com os itens")
public record NotaResponse(
        Long id,
        String chaveAcesso,
        MercadoResponse mercado,
        LocalDateTime dataEmissao,
        BigDecimal valorTotal,
        BigDecimal descontos,
        BigDecimal valorPago,
        List<ItemNotaResponse> itens
) {
}
