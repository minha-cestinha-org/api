package minhacestinha.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tokens de acesso")
public record LoginResponseDTO(
        @Schema(description = "Access token (1 hora)")
        String token,

        @Schema(description = "Refresh token (30 dias)")
        String refreshToken
) {
}
