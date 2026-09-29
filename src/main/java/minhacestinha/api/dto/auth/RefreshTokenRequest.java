package minhacestinha.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token para renovar o acesso")
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token é obrigatório.")
        String refreshToken
) {
}
