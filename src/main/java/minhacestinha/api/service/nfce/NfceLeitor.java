package minhacestinha.api.service.nfce;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** QR code (ou chave digitada) → nota lida, usando o parser do estado da nota. */
@Service
public class NfceLeitor {

    private final Map<String, NfceParser> parsersPorUf;
    private final SefazClient sefazClient;

    public NfceLeitor(List<NfceParser> parsers, SefazClient sefazClient) {
        this.parsersPorUf = parsers.stream().collect(Collectors.toMap(NfceParser::uf, Function.identity()));
        this.sefazClient = sefazClient;
    }

    public NotaLida ler(QrCodeNfce qrCode) {
        NfceParser parser = parsersPorUf.get(qrCode.chave().uf());
        if (parser == null) {
            throw NfceException.ufNaoSuportada(qrCode.chave().uf());
        }
        return parser.parse(sefazClient.buscar(parser.urlConsulta(qrCode)), qrCode);
    }
}
