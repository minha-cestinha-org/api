package minhacestinha.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Código recebido por e-mail e a senha nova")
public record RedefinirSenhaRequest(
        @Schema(example = "voce@email.com")
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email,

        @Schema(example = "482913")
        @NotBlank(message = "Código é obrigatório.")
        @Pattern(regexp = "\\d{6}", message = "O código tem 6 números.")
        String codigo,

        @Schema(example = "senha-nova-123")
        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres.")
        String novaSenha
) {
}
