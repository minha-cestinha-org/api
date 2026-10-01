package minhacestinha.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Preferências de privacidade do usuário")
public record PrivacidadeRequest(
        @Schema(description = "Compartilhar os preços, sem identificar o usuário, no \"preço da galera\"", example = "true")
        @NotNull(message = "Informe se quer compartilhar os preços.")
        Boolean compartilharPrecos
) {
}
