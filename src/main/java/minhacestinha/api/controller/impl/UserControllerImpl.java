package minhacestinha.api.controller.impl;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.controller.UserController;
import minhacestinha.api.dto.response.DadosUsuarioResponse;
import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> me(User usuario) {
        return ResponseEntity.ok(userService.me(usuario));
    }

    @Override
    public ResponseEntity<DadosUsuarioResponse> exportarDados(User usuario) {
        return ResponseEntity.ok(userService.exportarDados(usuario));
    }

    @Override
    public ResponseEntity<Void> apagarConta(User usuario) {
        userService.apagarConta(usuario);
        return ResponseEntity.noContent().build();
    }
}
