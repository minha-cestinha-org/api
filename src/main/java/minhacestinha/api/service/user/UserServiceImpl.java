package minhacestinha.api.service.user;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.dto.auth.RegistrationDTO;
import minhacestinha.api.dto.response.DadosUsuarioResponse;
import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.entity.UserRole;
import minhacestinha.api.persistence.mapper.NotaMapper;
import minhacestinha.api.persistence.mapper.UserMapper;
import minhacestinha.api.persistence.repository.ItemNotaRepository;
import minhacestinha.api.persistence.repository.NotaRepository;
import minhacestinha.api.persistence.repository.ProdutoUsuarioRepository;
import minhacestinha.api.persistence.repository.UserRepository;
import minhacestinha.api.service.produto.ProdutoService;
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
    private final NotaRepository notaRepository;
    private final ItemNotaRepository itemNotaRepository;
    private final ProdutoUsuarioRepository produtoUsuarioRepository;
    private final NotaMapper notaMapper;
    private final ProdutoService produtoService;
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

    @Override
    @Transactional(readOnly = true)
    public DadosUsuarioResponse exportarDados(User user) {
        return new DadosUsuarioResponse(
                userMapper.toResponse(user),
                user.getTermosAceitosEm(),
                notaRepository.findByUsuarioIdOrderByDataEmissaoDesc(user.getId()).stream().map(notaMapper::toResponse).toList(),
                produtoService.listar(user));
    }

    @Override
    @Transactional
    public void apagarConta(User user) {
        itemNotaRepository.apagarDoUsuario(user.getId());
        notaRepository.apagarDoUsuario(user.getId());
        produtoUsuarioRepository.apagarDoUsuario(user.getId());
        userRepository.deleteById(user.getId());
    }
}
