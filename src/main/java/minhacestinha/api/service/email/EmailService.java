package minhacestinha.api.service.email;

public interface EmailService {

    /** Manda o código de redefinição de senha. Se o e-mail não estiver configurado ou falhar, só loga. */
    void enviarCodigoRedefinicao(String para, String nome, String codigo);
}
