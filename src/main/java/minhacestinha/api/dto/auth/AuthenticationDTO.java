package minhacestinha.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record AuthenticationDTO(
        @Schema(example = "voce@email.com")
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email,

        @Schema(example = "senha-forte-123")
        @NotBlank(message = "Senha é obrigatória.")
        String senha
) {
}
