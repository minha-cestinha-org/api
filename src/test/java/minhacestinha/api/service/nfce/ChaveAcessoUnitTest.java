package minhacestinha.api.service.nfce;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChaveAcessoUnitTest {

    @Test
    void decompoeChaveValida() {
        ChaveAcesso chave = ChaveAcesso.de("3526 0912 3456 7800 0190 6500 1000 0123 4510 0012 3455");

        assertThat(chave.valor()).isEqualTo(Fixtures.CHAVE_EXEMPLO);
        assertThat(chave.uf()).isEqualTo("SP");
        assertThat(chave.cnpjEmitente()).isEqualTo("12345678000190");
        assertThat(chave.modelo()).isEqualTo("65");
        assertThat(chave.serie()).isEqualTo(1);
        assertThat(chave.numero()).isEqualTo(12345L);
    }

    @Test
    void recusaDigitoVerificadorErrado() {
        String chaveErrada = Fixtures.CHAVE_EXEMPLO.substring(0, 43) + "0";

        assertThat(ChaveAcesso.valida(chaveErrada)).isFalse();
        assertThatThrownBy(() -> ChaveAcesso.de(chaveErrada))
                .isInstanceOf(NfceException.class)
                .extracting("codigoMensagem").isEqualTo("nfce.chave.invalida");
    }

    @Test
    void recusaTamanhoErrado() {
        assertThat(ChaveAcesso.valida("123")).isFalse();
        assertThat(ChaveAcesso.valida(null)).isFalse();
    }
}
