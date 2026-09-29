package minhacestinha.api.service.nfce;

import org.jsoup.nodes.Document;

/**
 * Cada estado tem um portal e um layout diferentes, então cada um ganha seu parser.
 * O parser é puro (HTML → nota) para ser testado com páginas salvas.
 */
public interface NfceParser {

    String uf();

    /** Monta a URL oficial de consulta a partir do QR code (nunca usa o host vindo do QR). */
    String urlConsulta(QrCodeNfce qrCode);

    NotaLida parse(Document html, QrCodeNfce qrCode);
}
