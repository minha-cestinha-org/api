package minhacestinha.api.service.user;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.dto.auth.RegistrationDTO;
import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.entity.UserRole;
import minhacestinha.api.persistence.mapper.UserMapper;
import minhacestinha.api.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final Mensagens mensagens;

    @Override
    @Transactional
    public UserResponse cadastrar(RegistrationDTO dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, mensagens.get("usuario.email.ja.cadastrado"));
        }

        User user = User.builder()
                .nome(dto.nome().trim())
                .email(email)
                .senha(passwordEncoder.encode(dto.senha()))
                .role(UserRole.USUARIO)
                .ativo(true)
                .termosAceitosEm(LocalDateTime.now())
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void atualizarUltimoLogin(User user) {
        user.setUltimoLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public UserResponse me(User user) {
        return userMapper.toResponse(user);
    }
}
