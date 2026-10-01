package minhacestinha.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.dto.response.InflacaoResponse;
import minhacestinha.api.persistence.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Gastos", description = "Quanto o usuário gastou no mercado")
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/gastos")
public interface GastoController {

    @GetMapping("/mensal")
    @Operation(summary = "Gastos por mês", description = "Total gasto em cada mês, do mais antigo ao atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gastos calculados"),
            @ApiResponse(responseCode = "400", description = "Quantidade de meses inválida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    ResponseEntity<List<GastoMensalResponse>> porMes(
            @Parameter(description = "Quantos meses (1 a 24)", example = "6")
            @RequestParam(defaultValue = "6") @Min(1) @Max(24) int meses,
            @Parameter(hidden = true) @AuthenticationPrincipal User usuario);

    @GetMapping("/inflacao")
    @Operation(summary = "Inflação pessoal",
            description = "Quanto subiram os produtos que o usuário compra no período, comparado ao IPCA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inflação calculada"),
            @ApiResponse(responseCode = "400", description = "Quantidade de meses inválida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    ResponseEntity<InflacaoResponse> inflacao(
            @Parameter(description = "Quantos meses (2 a 24)", example = "12")
            @RequestParam(defaultValue = "12") @Min(2) @Max(24) int meses,
            @Parameter(hidden = true) @AuthenticationPrincipal User usuario);
}
