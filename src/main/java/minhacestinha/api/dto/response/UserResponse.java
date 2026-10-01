package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do usuário")
public record UserResponse(
        Long id,
        String nome,
        String email,
        @Schema(description = "Se o usuário topou compartilhar os preços (sem identificação) no \"preço da galera\"")
        boolean compartilharPrecos
) {
}
