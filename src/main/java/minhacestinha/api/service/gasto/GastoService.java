package minhacestinha.api.service.gasto;

import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.dto.response.InflacaoResponse;
import minhacestinha.api.persistence.entity.User;

import java.util.List;

public interface GastoService {

    /** Total gasto por mês nos últimos {@code meses} meses (incluindo o atual), do mais antigo ao mais recente. */
    List<GastoMensalResponse> porMes(User usuario, int meses);

    /** Quanto subiram os produtos que o usuário compra nos últimos {@code meses} meses, comparado ao IPCA. */
    InflacaoResponse inflacao(User usuario, int meses);
}
