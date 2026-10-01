package minhacestinha.api.service.user;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.dto.auth.RedefinirSenhaRequest;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.RedefinicaoSenha;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.RedefinicaoSenhaRepository;
import minhacestinha.api.persistence.repository.UserRepository;
import minhacestinha.api.service.email.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SenhaServiceImpl implements SenhaService {

    static final Duration VALIDADE = Duration.ofMinutes(15);
    static final Duration INTERVALO_MINIMO = Duration.ofMinutes(1);
    static final int MAX_TENTATIVAS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RedefinicaoSenhaRepository redefinicaoSenhaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TransactionTemplate transactionTemplate;
    private final Mensagens mensagens;
    private final Clock clock;

    /** O e-mail sai fora da transação. */
    @Override
    public void solicitarRedefinicao(String email) {
        User usuario = userRepository.findByEmail(normalizar(email)).filter(User::isEnabled).orElse(null);
        if (usuario == null) {
            return;
        }

        String codigo = transactionTemplate.execute(status -> gerarCodigo(usuario));
        if (codigo != null) {
            emailService.enviarCodigoRedefinicao(usuario.getEmail(), usuario.getNome(), codigo);
        }
    }

    /** Erro de código não desfaz a contagem de tentativas. */
    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void redefinir(RedefinirSenhaRequest dto) {
        LocalDateTime agora = LocalDateTime.now(clock);
        User usuario = userRepository.findByEmail(normalizar(dto.email())).filter(User::isEnabled)
                .orElseThrow(this::codigoInvalido);
        RedefinicaoSenha redefinicao = redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(usuario.getId())
                .filter(pedido -> pedido.getUsadoEm() == null && pedido.getExpiraEm().isAfter(agora)
                        && pedido.getTentativas() < MAX_TENTATIVAS)
                .orElseThrow(this::codigoInvalido);

        if (!passwordEncoder.matches(dto.codigo(), redefinicao.getCodigoHash())) {
            redefinicao.setTentativas(redefinicao.getTentativas() + 1);
            redefinicaoSenhaRepository.save(redefinicao);
            throw codigoInvalido();
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuario.setSenhaAlteradaEm(agora);
        usuario.setVersaoToken(usuario.getVersaoToken() + 1);
        userRepository.save(usuario);
        redefinicaoSenhaRepository.invalidarPendentes(usuario.getId(), agora);
    }

    /** Devolve {@code null} se já mandou um código há menos de um minuto. */
    private String gerarCodigo(User usuario) {
        LocalDateTime agora = LocalDateTime.now(clock);
        boolean pediuAgorinha = redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(usuario.getId())
                .map(ultimo -> ultimo.getExpiraEm().minus(VALIDADE).plus(INTERVALO_MINIMO).isAfter(agora))
                .orElse(false);
        if (pediuAgorinha) {
            return null;
        }

        redefinicaoSenhaRepository.invalidarPendentes(usuario.getId(), agora);
        String codigo = "%06d".formatted(RANDOM.nextInt(1_000_000));
        redefinicaoSenhaRepository.save(RedefinicaoSenha.builder()
                .usuario(usuario)
                .codigoHash(passwordEncoder.encode(codigo))
                .expiraEm(agora.plus(VALIDADE))
                .build());
        return codigo;
    }

    private ResponseStatusException codigoInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagens.get("senha.codigo.invalido"));
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
