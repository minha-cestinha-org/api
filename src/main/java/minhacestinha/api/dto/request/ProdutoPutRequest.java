package minhacestinha.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Correção do nome do produto feita pelo usuário")
public record ProdutoPutRequest(
        @Schema(example = "leite integral itambé 1L")
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres.")
        String nome,

        @Schema(example = "itambé")
        @Size(max = 100, message = "Marca deve ter no máximo 100 caracteres.")
        String marca,

        @Schema(example = "frios e laticínios")
        @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres.")
        String categoria
) {
}
