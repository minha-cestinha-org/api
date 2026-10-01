package minhacestinha.api.controller.impl;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.controller.AuthController;
import minhacestinha.api.dto.auth.AuthenticationDTO;
import minhacestinha.api.dto.auth.EsqueciSenhaRequest;
import minhacestinha.api.dto.auth.LoginResponseDTO;
import minhacestinha.api.dto.auth.RedefinirSenhaRequest;
import minhacestinha.api.dto.auth.RefreshTokenRequest;
import minhacestinha.api.dto.auth.RegistrationDTO;
import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.UserRepository;
import minhacestinha.api.service.security.TokenService;
import minhacestinha.api.service.user.SenhaService;
import minhacestinha.api.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SenhaService senhaService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final Mensagens mensagens;

    @Override
    public ResponseEntity<UserResponse> register(RegistrationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.cadastrar(dto));
    }

    @Override
    public ResponseEntity<LoginResponseDTO> login(AuthenticationDTO dto) {
        var credenciais = new UsernamePasswordAuthenticationToken(dto.email().trim().toLowerCase(Locale.ROOT), dto.senha());
        var user = (User) authenticationManager.authenticate(credenciais).getPrincipal();

        userService.atualizarUltimoLogin(user);
        return ResponseEntity.ok(gerarTokens(user));
    }

    @Override
    public ResponseEntity<LoginResponseDTO> refresh(RefreshTokenRequest dto) {
        String email = tokenService.validateRefreshToken(dto.refreshToken().trim());
        User user = email == null ? null : userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .filter(encontrado -> tokenService.versaoValida(dto.refreshToken().trim(), encontrado))
                .orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, mensagens.get("erro.autenticacao"));
        }
        return ResponseEntity.ok(gerarTokens(user));
    }

    @Override
    public ResponseEntity<Void> esqueciSenha(EsqueciSenhaRequest dto) {
        senhaService.solicitarRedefinicao(dto.email());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> redefinirSenha(RedefinirSenhaRequest dto) {
        senhaService.redefinir(dto);
        return ResponseEntity.noContent().build();
    }

    private LoginResponseDTO gerarTokens(User user) {
        return new LoginResponseDTO(tokenService.generateToken(user), tokenService.generateRefreshToken(user));
    }
}
