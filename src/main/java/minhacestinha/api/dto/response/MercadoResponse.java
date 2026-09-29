package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mercado emissor da nota")
public record MercadoResponse(Long id, String cnpj, String nome, String endereco) {
}
