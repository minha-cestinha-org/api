package minhacestinha.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Pedido de código pra redefinir a senha")
public record EsqueciSenhaRequest(
        @Schema(example = "voce@email.com")
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email
) {
}
