package minhacestinha.api.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${email.remetente:minhacestinha <nao-responda@minhacestinha.com.br>}")
    private String remetente;

    @Override
    public void enviarCodigoRedefinicao(String para, String nome, String codigo) {
        JavaMailSender sender = host.isBlank() ? null : mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("E-mail não configurado (MAIL_HOST). O código de redefinição não foi enviado.");
            return;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject(codigo + " é o seu código da minhacestinha");
        mensagem.setText("""
                oi, %s!

                seu código pra criar uma senha nova é: %s

                ele vale por 15 minutos. se não foi você que pediu, pode ignorar este e-mail, sua senha continua a mesma.

                minhacestinha
                """.formatted(nome.toLowerCase(), codigo));
        try {
            sender.send(mensagem);
        } catch (MailException exception) {
            log.error("Não consegui enviar o e-mail de redefinição de senha: {}", exception.getMessage());
        }
    }
}
