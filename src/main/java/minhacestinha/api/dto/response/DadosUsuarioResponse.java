package minhacestinha.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Tudo que a minhacestinha guarda do usuário (LGPD, art. 18)")
public record DadosUsuarioResponse(
        UserResponse conta,
        @Schema(description = "Quando o usuário aceitou os termos de uso")
        LocalDateTime termosAceitosEm,
        @Schema(description = "Quando o usuário mudou o consentimento do \"preço da galera\" pela última vez")
        LocalDateTime compartilharPrecosEm,
        @Schema(description = "Todas as notas importadas, inclusive as removidas do histórico")
        List<NotaResponse> notas,
        @Schema(description = "Produtos comprados, com os nomes corrigidos pelo usuário")
        List<ProdutoResumoResponse> produtos
) {
}
