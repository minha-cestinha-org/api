package minhacestinha.api.service.nota;

import minhacestinha.api.dto.request.NotaQrCodeRequest;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.Nota;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.mapper.NotaMapper;
import minhacestinha.api.persistence.repository.MercadoRepository;
import minhacestinha.api.persistence.repository.NotaRepository;
import minhacestinha.api.service.nfce.Fixtures;
import minhacestinha.api.service.nfce.NfceLeitor;
import minhacestinha.api.service.produto.ProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotaServiceUnitTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private MercadoRepository mercadoRepository;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private NfceLeitor nfceLeitor;

    @Mock
    private NotaMapper notaMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private Mensagens mensagens;

    @InjectMocks
    private NotaServiceImpl service;

    private final User usuario = User.builder().id(1L).build();

    @Test
    void naoConsultaASefazQuandoANotaJaFoiImportada() {
        when(notaRepository.existsByUsuarioIdAndChaveAcesso(1L, Fixtures.CHAVE_EXEMPLO)).thenReturn(true);

        assertThatThrownBy(() -> service.importarPorQrCode(new NotaQrCodeRequest(Fixtures.QR_CODE_EXEMPLO), usuario))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verifyNoInteractions(nfceLeitor);
    }

    @Test
    void desativarFazSoftDelete() {
        Nota nota = Nota.builder().id(5L).usuario(usuario).ativo(true).build();
        when(notaRepository.findByIdAndUsuarioId(5L, 1L)).thenReturn(Optional.of(nota));

        service.desativar(5L, usuario);

        assertThat(nota.getAtivo()).isFalse();
        verify(notaRepository).save(nota);
    }

    @Test
    void naoEncontraNotaDeOutroUsuario() {
        when(notaRepository.findByIdAndUsuarioId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(5L, usuario))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
