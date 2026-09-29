package minhacestinha.api.service.nfce;

import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NfceSpParserUnitTest {

    private final NfceSpParser parser = new NfceSpParser();

    @Test
    void leNotaCompleta() {
        NotaLida nota = parser.parse(Fixtures.html("sp-nota-exemplo.html"), QrCodeNfce.ler(Fixtures.QR_CODE_EXEMPLO));

        assertThat(nota.chaveAcesso()).isEqualTo(Fixtures.CHAVE_EXEMPLO);
        assertThat(nota.uf()).isEqualTo("SP");
        assertThat(nota.mercado().nome()).isEqualTo("MERCADO DA VILA LTDA");
        assertThat(nota.mercado().cnpj()).isEqualTo("12345678000190");
        assertThat(nota.mercado().endereco()).isEqualTo("RUA DAS FLORES, 123, CENTRO, SAO PAULO, SP");
        assertThat(nota.dataEmissao()).isEqualTo(LocalDateTime.of(2026, 9, 26, 10, 15, 32));
        assertThat(nota.valorTotal()).isEqualByComparingTo("51.47");
        assertThat(nota.descontos()).isEqualByComparingTo("1.47");
        assertThat(nota.valorPago()).isEqualByComparingTo("50.00");
        assertThat(nota.avisos()).isEmpty();
        assertThat(nota.itens()).hasSize(4);
    }

    @Test
    void leItensComEanQuantidadeEUnidade() {
        NotaLida nota = parser.parse(Fixtures.html("sp-nota-exemplo.html"), QrCodeNfce.ler(Fixtures.QR_CODE_EXEMPLO));

        ItemLido leite = nota.itens().getFirst();
        assertThat(leite.numero()).isEqualTo(1);
        assertThat(leite.descricaoBruta()).isEqualTo("LTE COND INTEG ITAMB 1L");
        assertThat(leite.ean()).isEqualTo("7896051111115");
        assertThat(leite.quantidade()).isEqualByComparingTo("2");
        assertThat(leite.precoUnitario()).isEqualByComparingTo("7.90");
        assertThat(leite.precoTotal()).isEqualByComparingTo("15.80");

        ItemLido banana = nota.itens().get(1);
        assertThat(banana.codigo()).isEqualTo("1234");
        assertThat(banana.ean()).as("código interno não é EAN").isNull();
        assertThat(banana.quantidade()).isEqualByComparingTo("1.2");
        assertThat(banana.unidade()).isEqualTo("KG");
    }

    @Test
    void recusaPaginaDeOutraNota() {
        String outraChave = "35260812345678000190650010000111111000111115";

        assertThatThrownBy(() -> parser.parse(Fixtures.html("sp-nota-exemplo.html"), QrCodeNfce.ler(outraChave)))
                .isInstanceOf(NfceException.class)
                .extracting("codigoMensagem").isEqualTo("nfce.nao.encontrada");
    }

    @Test
    void identificaNotaNaoEncontrada() {
        assertThatThrownBy(() -> parser.parse(Fixtures.html("sp-nota-nao-encontrada.html"),
                QrCodeNfce.ler(Fixtures.QR_CODE_EXEMPLO)))
                .isInstanceOf(NfceException.class)
                .extracting("codigoMensagem").isEqualTo("nfce.nao.encontrada");
    }

    @Test
    void montaUrlOficialSemUsarOHostDoQrCode() {
        QrCodeNfce qrCode = QrCodeNfce.ler(Fixtures.QR_CODE_EXEMPLO.replace("www.nfce.fazenda.sp.gov.br", "evil.example.com"));

        assertThat(parser.urlConsulta(qrCode)).startsWith(
                "https://www.nfce.fazenda.sp.gov.br/NFCeConsultaPublica/Paginas/ConsultaQRCode.aspx?p=" + Fixtures.CHAVE_EXEMPLO + "|2|1|1|");
    }

    @Test
    void montaUrlDaVersao3QuandoSoTemAChave() {
        QrCodeNfce qrCode = QrCodeNfce.ler(Fixtures.CHAVE_EXEMPLO);

        assertThat(parser.urlConsulta(qrCode)).endsWith("?p=" + Fixtures.CHAVE_EXEMPLO + "|3|1");
    }
}
