package minhacestinha.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import minhacestinha.api.dto.auth.AuthenticationDTO;
import minhacestinha.api.dto.auth.LoginResponseDTO;
import minhacestinha.api.dto.auth.RefreshTokenRequest;
import minhacestinha.api.dto.auth.RegistrationDTO;
import minhacestinha.api.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Autenticação", description = "Cadastro, login e renovação de token")
@RequestMapping("/api/auth")
public interface AuthController {

    @PostMapping("/register")
    @Operation(summary = "Criar conta", description = "Cria uma conta. O aceite dos termos é obrigatório.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    ResponseEntity<UserResponse> register(@RequestBody @Valid RegistrationDTO dto);

    @PostMapping("/login")
    @Operation(summary = "Entrar", description = "Autentica com email e senha e devolve os tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado"),
            @ApiResponse(responseCode = "401", description = "Email ou senha inválidos")
    })
    ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO dto);

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Gera novos tokens a partir de um refresh token válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    ResponseEntity<LoginResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequest dto);
}
