package minhacestinha.api.service.nfce;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class Fixtures {

    public static final String CHAVE_EXEMPLO = "35260912345678000190650010000123451000123455";
    public static final String QR_CODE_EXEMPLO =
            "https://www.nfce.fazenda.sp.gov.br/NFCeConsultaPublica/Paginas/ConsultaQRCode.aspx?p="
                    + CHAVE_EXEMPLO + "|2|1|1|3A4B5C6D7E8F";

    private Fixtures() {
    }

    public static Document html(String arquivo) {
        try (InputStream in = Fixtures.class.getResourceAsStream("/nfce/" + arquivo)) {
            return Jsoup.parse(in, "UTF-8", "https://www.nfce.fazenda.sp.gov.br/");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
