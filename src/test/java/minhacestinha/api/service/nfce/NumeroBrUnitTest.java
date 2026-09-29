package minhacestinha.api.service.nfce;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumeroBrUnitTest {

    @Test
    void converteFormatoBrasileiro() {
        assertThat(NumeroBr.parse("1.234,56")).isEqualByComparingTo("1234.56");
        assertThat(NumeroBr.parse("0,345")).isEqualByComparingTo("0.345");
        assertThat(NumeroBr.parse("Vl. Unit.: 6,49")).isEqualByComparingTo("6.49");
        assertThat(NumeroBr.parse("6.49")).isEqualByComparingTo(new BigDecimal("6.49"));
    }

    @Test
    void recusaTextoSemNumero() {
        assertThatThrownBy(() -> NumeroBr.parse("abc")).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void validaGtin() {
        assertThat(NumeroBr.gtinValido("7891000100103")).isTrue();
        assertThat(NumeroBr.gtinValido("7891000100104")).isFalse();
        assertThat(NumeroBr.gtinValido("0000000000000")).isFalse();
        assertThat(NumeroBr.gtinValido("1234")).isFalse();
    }

    @Test
    void normalizaGtinParaEan13() {
        assertThat(NumeroBr.normalizarGtin("789100010010")).isEqualTo("0789100010010");
        assertThat(NumeroBr.normalizarGtin("07891000100103")).isEqualTo("7891000100103");
        assertThat(NumeroBr.normalizarGtin("7891000100103")).isEqualTo("7891000100103");
    }
}
