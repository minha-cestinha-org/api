package minhacestinha.api.service.nfce;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Falha na leitura da nota. Carrega o código da mensagem (messages.properties) e o status HTTP,
 * pra o parser não depender do Spring MVC nem das mensagens.
 */
@Getter
public class NfceException extends RuntimeException {

    private final HttpStatus status;
    private final String codigoMensagem;
    private final transient Object[] args;

    public NfceException(HttpStatus status, String codigoMensagem, Object... args) {
        super(codigoMensagem);
        this.status = status;
        this.codigoMensagem = codigoMensagem;
        this.args = args;
    }

    public static NfceException qrCodeInvalido() {
        return new NfceException(HttpStatus.BAD_REQUEST, "nfce.qrcode.invalido");
    }

    public static NfceException chaveInvalida() {
        return new NfceException(HttpStatus.BAD_REQUEST, "nfce.chave.invalida");
    }

    public static NfceException ufNaoSuportada(String uf) {
        return new NfceException(HttpStatus.UNPROCESSABLE_CONTENT, "nfce.uf.nao.suportada", uf);
    }

    public static NfceException sefazIndisponivel() {
        return new NfceException(HttpStatus.BAD_GATEWAY, "nfce.sefaz.indisponivel");
    }

    public static NfceException naoEncontrada() {
        return new NfceException(HttpStatus.NOT_FOUND, "nfce.nao.encontrada");
    }

    public static NfceException layoutDesconhecido() {
        return new NfceException(HttpStatus.BAD_GATEWAY, "nfce.layout.desconhecido");
    }
}
