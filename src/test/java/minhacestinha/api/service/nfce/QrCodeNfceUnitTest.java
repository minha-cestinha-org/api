package minhacestinha.api.service.nfce;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QrCodeNfceUnitTest {

    @Test
    void leUrlDaVersao2() {
        QrCodeNfce qrCode = QrCodeNfce.ler(Fixtures.QR_CODE_EXEMPLO);

        assertThat(qrCode.chave().valor()).isEqualTo(Fixtures.CHAVE_EXEMPLO);
        assertThat(qrCode.parametroP()).isEqualTo(Fixtures.CHAVE_EXEMPLO + "|2|1|1|3A4B5C6D7E8F");
        assertThat(qrCode.ambiente()).isEqualTo(1);
    }

    @Test
    void leUrlComPipeCodificado() {
        QrCodeNfce qrCode = QrCodeNfce.ler(Fixtures.QR_CODE_EXEMPLO.replace("|", "%7C"));

        assertThat(qrCode.chave().valor()).isEqualTo(Fixtures.CHAVE_EXEMPLO);
    }

    @Test
    void leUrlDaVersao1ComHomologacao() {
        QrCodeNfce qrCode = QrCodeNfce.ler("https://www.homologacao.nfce.fazenda.sp.gov.br/qrcode?chNFe="
                + Fixtures.CHAVE_EXEMPLO + "&nVersao=100&tpAmb=2");

        assertThat(qrCode.chave().valor()).isEqualTo(Fixtures.CHAVE_EXEMPLO);
        assertThat(qrCode.ambiente()).isEqualTo(2);
    }

    @Test
    void aceitaChaveDigitadaComEspacos() {
        QrCodeNfce qrCode = QrCodeNfce.ler("3526 0912 3456 7800 0190 6500 1000 0123 4510 0012 3455");

        assertThat(qrCode.chave().valor()).isEqualTo(Fixtures.CHAVE_EXEMPLO);
        assertThat(qrCode.parametroP()).isNull();
    }

    @Test
    void recusaConteudoQueNaoEhNota() {
        assertThatThrownBy(() -> QrCodeNfce.ler("https://exemplo.com.br/promo"))
                .isInstanceOf(NfceException.class)
                .extracting("codigoMensagem").isEqualTo("nfce.qrcode.invalido");
        assertThatThrownBy(() -> QrCodeNfce.ler("oi"))
                .isInstanceOf(NfceException.class);
    }
}
