package minhacestinha.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import minhacestinha.api.dto.request.NotaQrCodeRequest;
import minhacestinha.api.dto.response.NotaResponse;
import minhacestinha.api.dto.response.NotaResumoResponse;
import minhacestinha.api.persistence.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Notas", description = "Importação e consulta das notas fiscais (NFC-e)")
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/notas")
public interface NotaController {

    @PostMapping("/qrcode")
    @Operation(summary = "Importar nota pelo QR code",
            description = "Lê a NFC-e na Sefaz a partir do conteúdo do QR code (ou da chave de acesso) e salva a compra. Por enquanto, só SP.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nota importada"),
            @ApiResponse(responseCode = "400", description = "QR code ou chave inválida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "A Sefaz não encontrou a nota"),
            @ApiResponse(responseCode = "409", description = "Nota já importada"),
            @ApiResponse(responseCode = "422", description = "Estado ainda não suportado"),
            @ApiResponse(responseCode = "502", description = "Sefaz indisponível ou com layout desconhecido")
    })
    ResponseEntity<NotaResponse> importarPorQrCode(@RequestBody @Valid NotaQrCodeRequest dto,
                                                   @Parameter(hidden = true) @AuthenticationPrincipal User usuario);

    @GetMapping
    @Operation(summary = "Listar compras", description = "Lista as notas do usuário, da mais recente para a mais antiga.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notas encontradas"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    ResponseEntity<List<NotaResumoResponse>> listar(@Parameter(hidden = true) @AuthenticationPrincipal User usuario);

    @GetMapping("/{id}")
    @Operation(summary = "Buscar nota", description = "Retorna a nota com todos os itens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nota encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    })
    ResponseEntity<NotaResponse> buscar(@Parameter(description = "ID da nota", example = "1") @PathVariable Long id,
                                        @Parameter(hidden = true) @AuthenticationPrincipal User usuario);

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Remover nota", description = "Remove a nota do histórico (soft delete).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Nota removida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    })
    ResponseEntity<Void> desativar(@Parameter(description = "ID da nota", example = "1") @PathVariable Long id,
                                   @Parameter(hidden = true) @AuthenticationPrincipal User usuario);
}
