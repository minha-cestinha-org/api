package minhacestinha.api.service.gasto;

import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.persistence.entity.User;

import java.util.List;

public interface GastoService {

    /** Total gasto por mês nos últimos {@code meses} meses (incluindo o atual), do mais antigo ao mais recente. */
    List<GastoMensalResponse> porMes(User usuario, int meses);
}
