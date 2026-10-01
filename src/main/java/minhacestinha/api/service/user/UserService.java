package minhacestinha.api.service.user;

import minhacestinha.api.dto.auth.RegistrationDTO;
import minhacestinha.api.dto.request.PrivacidadeRequest;
import minhacestinha.api.dto.response.DadosUsuarioResponse;
import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.persistence.entity.User;

public interface UserService {

    UserResponse cadastrar(RegistrationDTO dto);

    void atualizarUltimoLogin(User user);

    UserResponse me(User user);

    /** Liga ou desliga o "preço da galera" e guarda quando mudou. */
    UserResponse atualizarPrivacidade(User user, PrivacidadeRequest dto);

    /** Exporta tudo que guardamos do usuário. */
    DadosUsuarioResponse exportarDados(User user);

    /** Apaga a conta e tudo que é do usuário. Mercados e produtos do catálogo ficam, porque não identificam ninguém. */
    void apagarConta(User user);
}
