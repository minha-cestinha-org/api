package minhacestinha.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import minhacestinha.api.dto.request.ProdutoPutRequest;
import minhacestinha.api.dto.response.ProdutoHistoricoResponse;
import minhacestinha.api.dto.response.ProdutoResumoResponse;
import minhacestinha.api.persistence.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Produtos", description = "Preços e histórico dos produtos que o usuário já comprou")
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/produtos")
public interface ProdutoController {

    @GetMapping
    @Operation(summary = "Listar preços", description = "Produtos já comprados, com último preço, variação e menor preço (os cards de preços).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos encontrados"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    ResponseEntity<List<ProdutoResumoResponse>> listar(@Parameter(hidden = true) @AuthenticationPrincipal User usuario);

    @GetMapping("/{id}/historico")
    @Operation(summary = "Histórico do produto", description = "Resumo e todas as compras do produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado nas compras do usuário")
    })
    ResponseEntity<ProdutoHistoricoResponse> historico(@Parameter(description = "ID do produto", example = "1") @PathVariable Long id,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal User usuario);

    @GetMapping("/ean/{ean}")
    @Operation(summary = "Modo mercado", description = "Histórico do usuário para o produto do código de barras lido na gôndola.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico encontrado"),
            @ApiResponse(responseCode = "400", description = "Código de barras inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "O usuário ainda não comprou esse produto")
    })
    ResponseEntity<ProdutoHistoricoResponse> historicoPorEan(
            @Parameter(description = "Código de barras (EAN/GTIN)", example = "7891000100103") @PathVariable String ean,
            @Parameter(hidden = true) @AuthenticationPrincipal User usuario);

    @PutMapping("/{id}")
    @Operation(summary = "Corrigir produto", description = "Corrige nome, marca e categoria de um produto que o usuário já comprou. A correção vale só pra ele.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado nas compras do usuário")
    })
    ResponseEntity<Void> atualizar(@Parameter(description = "ID do produto", example = "1") @PathVariable Long id,
                                   @RequestBody @Valid ProdutoPutRequest dto,
                                   @Parameter(hidden = true) @AuthenticationPrincipal User usuario);
}
