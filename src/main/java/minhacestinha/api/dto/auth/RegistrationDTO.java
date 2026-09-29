package minhacestinha.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar uma conta")
public record RegistrationDTO(
        @Schema(example = "Eduardo")
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String nome,

        @Schema(example = "voce@email.com")
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres.")
        String email,

        @Schema(example = "senha-forte-123")
        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @Schema(description = "Aceite dos termos de uso e da política de privacidade", example = "true")
        @AssertTrue(message = "É preciso aceitar os termos de uso e a política de privacidade.")
        boolean aceitouTermos
) {
}
