package minhacestinha.api.service.nfce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Nota como veio da Sefaz, antes de qualquer padronização ou persistência. */
public record NotaLida(
        String chaveAcesso,
        String uf,
        MercadoLido mercado,
        LocalDateTime dataEmissao,
        BigDecimal valorTotal,
        BigDecimal descontos,
        BigDecimal valorPago,
        List<ItemLido> itens,
        List<String> avisos
) {

    public record MercadoLido(String cnpj, String nome, String endereco) {
    }

    public record ItemLido(
            int numero,
            String descricaoBruta,
            String codigo,
            String ean,
            BigDecimal quantidade,
            String unidade,
            BigDecimal precoUnitario,
            BigDecimal precoTotal
    ) {
    }
}
