package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do usuário")
public record UserResponse(Long id, String nome, String email) {
}
