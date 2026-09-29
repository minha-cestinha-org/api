package minhacestinha.api.service.nfce;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Baixa a página de consulta da NFC-e no portal da Sefaz. */
@Slf4j
@Component
public class SefazClient {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0 Mobile Safari/537.36";

    @Value("${sefaz.timeout-ms:15000}")
    private int timeoutMs;

    public Document buscar(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept-Language", "pt-BR")
                    .timeout(timeoutMs)
                    .followRedirects(true)
                    .get();
        } catch (IOException exception) {
            log.warn("Falha ao consultar a Sefaz: {}", exception.getMessage());
            throw NfceException.sefazIndisponivel();
        }
    }
}
