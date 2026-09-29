package minhacestinha.api.controller.impl;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.controller.NotaController;
import minhacestinha.api.dto.request.NotaQrCodeRequest;
import minhacestinha.api.dto.response.NotaResponse;
import minhacestinha.api.dto.response.NotaResumoResponse;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.service.nota.NotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotaControllerImpl implements NotaController {

    private final NotaService notaService;

    @Override
    public ResponseEntity<NotaResponse> importarPorQrCode(NotaQrCodeRequest dto, User usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notaService.importarPorQrCode(dto, usuario));
    }

    @Override
    public ResponseEntity<List<NotaResumoResponse>> listar(User usuario) {
        return ResponseEntity.ok(notaService.listar(usuario));
    }

    @Override
    public ResponseEntity<NotaResponse> buscar(Long id, User usuario) {
        return ResponseEntity.ok(notaService.buscar(id, usuario));
    }

    @Override
    public ResponseEntity<Void> desativar(Long id, User usuario) {
        notaService.desativar(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
