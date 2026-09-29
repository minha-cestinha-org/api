package minhacestinha.api.service.nfce;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Conteúdo lido do QR code da NFC-e.
 *
 * <p>Aceita a URL do QR code (versões 1, 2 e 3) ou só a chave de acesso digitada. A URL nunca é usada
 * para buscar a nota: o parser de cada estado monta a URL oficial a partir da chave (evita SSRF).</p>
 *
 * @param parametroP valor do parâmetro {@code p} (chave|versão|ambiente|...), ou {@code null} quando veio só a chave
 * @param ambiente   1 = produção, 2 = homologação
 */
public record QrCodeNfce(ChaveAcesso chave, String parametroP, int ambiente) {

    public static QrCodeNfce ler(String conteudo) {
        String texto = conteudo == null ? "" : conteudo.trim();
        String soDigitos = texto.replaceAll("\\s", "");
        if (soDigitos.matches("\\d{44}")) {
            return new QrCodeNfce(ChaveAcesso.de(soDigitos), null, 1);
        }

        Map<String, String> parametros = parametrosDaUrl(texto);

        String p = parametros.get("p");
        if (p != null) {
            String[] campos = p.split("\\|");
            int ambiente = campos.length > 2 && "2".equals(campos[2]) ? 2 : 1;
            return new QrCodeNfce(ChaveAcesso.de(campos[0]), p, ambiente);
        }

        String chNFe = parametros.get("chNFe");
        if (chNFe != null) {
            int ambiente = "2".equals(parametros.get("tpAmb")) ? 2 : 1;
            return new QrCodeNfce(ChaveAcesso.de(chNFe), null, ambiente);
        }

        throw NfceException.qrCodeInvalido();
    }

    private static Map<String, String> parametrosDaUrl(String texto) {
        String query;
        try {
            query = Optional.ofNullable(URI.create(texto.replace("|", "%7C")).getRawQuery())
                    .orElseThrow(NfceException::qrCodeInvalido);
        } catch (IllegalArgumentException exception) {
            throw NfceException.qrCodeInvalido();
        }
        return Arrays.stream(query.split("&"))
                .map(par -> par.split("=", 2))
                .filter(par -> par.length == 2)
                .collect(Collectors.toMap(
                        par -> par[0],
                        par -> URLDecoder.decode(par[1], StandardCharsets.UTF_8),
                        (primeiro, segundo) -> primeiro));
    }
}
