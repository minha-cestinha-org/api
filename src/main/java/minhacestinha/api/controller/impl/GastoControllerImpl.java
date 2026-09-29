package minhacestinha.api.controller.impl;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.controller.GastoController;
import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.service.gasto.GastoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GastoControllerImpl implements GastoController {

    private final GastoService gastoService;

    @Override
    public ResponseEntity<List<GastoMensalResponse>> porMes(int meses, User usuario) {
        return ResponseEntity.ok(gastoService.porMes(usuario, meses));
    }
}
