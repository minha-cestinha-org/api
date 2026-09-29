package minhacestinha.api.service.nota;

import minhacestinha.api.dto.request.NotaQrCodeRequest;
import minhacestinha.api.dto.response.NotaResponse;
import minhacestinha.api.dto.response.NotaResumoResponse;
import minhacestinha.api.persistence.entity.User;

import java.util.List;

public interface NotaService {

    NotaResponse importarPorQrCode(NotaQrCodeRequest dto, User usuario);

    List<NotaResumoResponse> listar(User usuario);

    NotaResponse buscar(Long id, User usuario);

    void desativar(Long id, User usuario);
}
