package minhacestinha.api.service.user;

import minhacestinha.api.dto.auth.RegistrationDTO;
import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.persistence.entity.User;

public interface UserService {

    UserResponse cadastrar(RegistrationDTO dto);

    void atualizarUltimoLogin(User user);

    UserResponse me(User user);
}
