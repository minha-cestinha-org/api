package minhacestinha.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.persistence.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Usuários", description = "Dados da conta")
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/users")
public interface UserController {

    @GetMapping("/me")
    @Operation(summary = "Minha conta", description = "Retorna os dados do usuário logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    ResponseEntity<UserResponse> me(@Parameter(hidden = true) @AuthenticationPrincipal User usuario);
}
