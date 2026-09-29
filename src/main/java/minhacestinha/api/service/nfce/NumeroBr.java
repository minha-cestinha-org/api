package minhacestinha.api.service.nfce;

import java.math.BigDecimal;

/** Utilitários de números em formato brasileiro e códigos de barras. */
public final class NumeroBr {

    private NumeroBr() {
    }

    /** "1.234,56" → 1234.56; "0,345" → 0.345; aceita ponto como decimal quando não há vírgula. */
    public static BigDecimal parse(String texto) {
        String limpo = texto == null ? "" : texto.replaceAll("[^\\d.,-]", "");
        if (limpo.isEmpty()) {
            throw new NumberFormatException("Número vazio: " + texto);
        }
        String normalizado = limpo.contains(",") ? limpo.replace(".", "").replace(",", ".") : limpo;
        return new BigDecimal(normalizado);
    }

    /** Valida o dígito verificador de um GTIN-8, 12, 13 ou 14. Códigos só com zeros não contam. */
    public static boolean gtinValido(String codigo) {
        if (codigo == null || !codigo.matches("\\d{8}|\\d{12,14}") || codigo.matches("0+")) {
            return false;
        }
        int soma = 0;
        for (int i = codigo.length() - 2, posicao = 0; i >= 0; i--, posicao++) {
            soma += Character.getNumericValue(codigo.charAt(i)) * (posicao % 2 == 0 ? 3 : 1);
        }
        int dv = (10 - soma % 10) % 10;
        return dv == Character.getNumericValue(codigo.charAt(codigo.length() - 1));
    }

    /** Normaliza GTIN-12 (UPC) e GTIN-14 com zero à esquerda para EAN-13. */
    public static String normalizarGtin(String codigo) {
        if (codigo.length() == 12) {
            return "0" + codigo;
        }
        if (codigo.length() == 14 && codigo.startsWith("0")) {
            return codigo.substring(1);
        }
        return codigo;
    }
}
