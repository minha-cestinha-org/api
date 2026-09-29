package minhacestinha.api.service.nfce;

import java.util.Map;

/**
 * Chave de acesso da NFC-e (44 dígitos): cUF(2) AAMM(4) CNPJ(14) modelo(2) série(3) número(9) tpEmis(1) cNF(8) DV(1).
 */
public record ChaveAcesso(String valor, String uf, String cnpjEmitente, String modelo, int serie, long numero) {

    private static final Map<String, String> UF_POR_CODIGO = Map.ofEntries(
            Map.entry("11", "RO"), Map.entry("12", "AC"), Map.entry("13", "AM"), Map.entry("14", "RR"),
            Map.entry("15", "PA"), Map.entry("16", "AP"), Map.entry("17", "TO"), Map.entry("21", "MA"),
            Map.entry("22", "PI"), Map.entry("23", "CE"), Map.entry("24", "RN"), Map.entry("25", "PB"),
            Map.entry("26", "PE"), Map.entry("27", "AL"), Map.entry("28", "SE"), Map.entry("29", "BA"),
            Map.entry("31", "MG"), Map.entry("32", "ES"), Map.entry("33", "RJ"), Map.entry("35", "SP"),
            Map.entry("41", "PR"), Map.entry("42", "SC"), Map.entry("43", "RS"), Map.entry("50", "MS"),
            Map.entry("51", "MT"), Map.entry("52", "GO"), Map.entry("53", "DF"));

    public static ChaveAcesso de(String entrada) {
        String chave = entrada == null ? "" : entrada.replaceAll("\\s", "");
        if (!valida(chave)) {
            throw NfceException.chaveInvalida();
        }
        String uf = UF_POR_CODIGO.get(chave.substring(0, 2));
        if (uf == null) {
            throw NfceException.chaveInvalida();
        }
        return new ChaveAcesso(
                chave,
                uf,
                chave.substring(6, 20),
                chave.substring(20, 22),
                Integer.parseInt(chave.substring(22, 25)),
                Long.parseLong(chave.substring(25, 34)));
    }

    public static boolean valida(String chave) {
        return chave != null
                && chave.matches("\\d{44}")
                && digitoVerificador(chave.substring(0, 43)) == Character.getNumericValue(chave.charAt(43));
    }

    /** Módulo 11 com pesos de 2 a 9, da direita para a esquerda. */
    static int digitoVerificador(String chave43) {
        int soma = 0;
        int peso = 2;
        for (int i = chave43.length() - 1; i >= 0; i--) {
            soma += Character.getNumericValue(chave43.charAt(i)) * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
