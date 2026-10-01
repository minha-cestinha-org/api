package minhacestinha.api.service.user;

import minhacestinha.api.dto.auth.RedefinirSenhaRequest;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.RedefinicaoSenha;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.RedefinicaoSenhaRepository;
import minhacestinha.api.persistence.repository.UserRepository;
import minhacestinha.api.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SenhaServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedefinicaoSenhaRepository redefinicaoSenhaRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private Mensagens mensagens;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-29T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private final LocalDateTime agora = LocalDateTime.now(clock);

    private SenhaServiceImpl service;
    private User usuario;

    @BeforeEach
    void setUp() {
        service = new SenhaServiceImpl(userRepository, redefinicaoSenhaRepository, passwordEncoder, emailService,
                transactionTemplate, mensagens, clock);
        usuario = User.builder().id(1L).nome("Eduardo").email("edu@email.com").senha("antiga").ativo(true).build();
        lenient().when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> invocation.<TransactionCallback<?>>getArgument(0).doInTransaction(null));
    }

    @Test
    void mandaCodigoDeSeisNumerosEGuardaSoOHash() {
        when(userRepository.findByEmail("edu@email.com")).thenReturn(Optional.of(usuario));
        when(redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.empty());

        service.solicitarRedefinicao(" Edu@Email.com ");

        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarCodigoRedefinicao(eq("edu@email.com"), eq("Eduardo"), codigo.capture());
        assertThat(codigo.getValue()).matches("\\d{6}");

        ArgumentCaptor<RedefinicaoSenha> salvo = ArgumentCaptor.forClass(RedefinicaoSenha.class);
        verify(redefinicaoSenhaRepository).save(salvo.capture());
        assertThat(salvo.getValue().getCodigoHash()).isNotEqualTo(codigo.getValue());
        assertThat(passwordEncoder.matches(codigo.getValue(), salvo.getValue().getCodigoHash())).isTrue();
        assertThat(salvo.getValue().getExpiraEm()).isEqualTo(agora.plusMinutes(15));
    }

    @Test
    void emailSemContaNaoFazNadaENaoDaErro() {
        when(userRepository.findByEmail("ninguem@email.com")).thenReturn(Optional.empty());

        service.solicitarRedefinicao("ninguem@email.com");

        verify(emailService, never()).enviarCodigoRedefinicao(anyString(), anyString(), anyString());
    }

    @Test
    void naoMandaOutroCodigoEmMenosDeUmMinuto() {
        when(userRepository.findByEmail("edu@email.com")).thenReturn(Optional.of(usuario));
        when(redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(1L))
                .thenReturn(Optional.of(pedido("123456", agora.plusMinutes(15).minusSeconds(30))));

        service.solicitarRedefinicao("edu@email.com");

        verify(emailService, never()).enviarCodigoRedefinicao(anyString(), anyString(), anyString());
        verify(redefinicaoSenhaRepository, never()).save(any());
    }

    @Test
    void codigoCertoTrocaASenhaEDerrubaOsTokens() {
        RedefinicaoSenha pedido = pedido("123456", agora.plusMinutes(10));
        when(userRepository.findByEmail("edu@email.com")).thenReturn(Optional.of(usuario));
        when(redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.of(pedido));

        service.redefinir(new RedefinirSenhaRequest("edu@email.com", "123456", "senha-nova-123"));

        assertThat(passwordEncoder.matches("senha-nova-123", usuario.getSenha())).isTrue();
        assertThat(usuario.getVersaoToken()).isEqualTo(1);
        assertThat(usuario.getSenhaAlteradaEm()).isEqualTo(agora);
        verify(redefinicaoSenhaRepository).invalidarPendentes(1L, agora);
    }

    @Test
    void codigoErradoContaTentativa() {
        RedefinicaoSenha pedido = pedido("123456", agora.plusMinutes(10));
        when(userRepository.findByEmail("edu@email.com")).thenReturn(Optional.of(usuario));
        when(redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.of(pedido));

        assertBadRequest(new RedefinirSenhaRequest("edu@email.com", "000000", "senha-nova-123"));

        assertThat(pedido.getTentativas()).isEqualTo(1);
        assertThat(usuario.getSenha()).isEqualTo("antiga");
    }

    @Test
    void codigoExpiradoOuComTentativasDemaisNaoVale() {
        when(userRepository.findByEmail("edu@email.com")).thenReturn(Optional.of(usuario));

        when(redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(1L))
                .thenReturn(Optional.of(pedido("123456", agora.minusSeconds(1))));
        assertBadRequest(new RedefinirSenhaRequest("edu@email.com", "123456", "senha-nova-123"));

        RedefinicaoSenha esgotado = pedido("123456", agora.plusMinutes(10));
        esgotado.setTentativas(5);
        when(redefinicaoSenhaRepository.findFirstByUsuarioIdOrderByIdDesc(1L)).thenReturn(Optional.of(esgotado));
        assertBadRequest(new RedefinirSenhaRequest("edu@email.com", "123456", "senha-nova-123"));

        assertThat(usuario.getSenha()).isEqualTo("antiga");
    }

    private void assertBadRequest(RedefinirSenhaRequest dto) {
        assertThatThrownBy(() -> service.redefinir(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private RedefinicaoSenha pedido(String codigo, LocalDateTime expiraEm) {
        return RedefinicaoSenha.builder().id(9L).usuario(usuario).codigoHash(passwordEncoder.encode(codigo))
                .expiraEm(expiraEm).build();
    }
}
