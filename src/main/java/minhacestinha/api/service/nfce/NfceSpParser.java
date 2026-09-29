package minhacestinha.api.service.nfce;

import minhacestinha.api.service.nfce.NotaLida.ItemLido;
import minhacestinha.api.service.nfce.NotaLida.MercadoLido;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Leitura da página de consulta da NFC-e da Sefaz-SP (layout padrão do portal NFC-e).
 */
@Component
public class NfceSpParser implements NfceParser {

    private static final String HOST_PRODUCAO = "https://www.nfce.fazenda.sp.gov.br";
    private static final String HOST_HOMOLOGACAO = "https://www.homologacao.nfce.fazenda.sp.gov.br";
    private static final String CAMINHO_QRCODE = "/NFCeConsultaPublica/Paginas/ConsultaQRCode.aspx";

    private static final Pattern DATA_EMISSAO =
            Pattern.compile("Emiss[ãa]o:\\s*(\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}:\\d{2})", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Diferença aceitável (R$) entre a soma dos itens e o total antes de gerar aviso. */
    private static final BigDecimal TOLERANCIA_TOTAL = new BigDecimal("0.05");

    @Override
    public String uf() {
        return "SP";
    }

    @Override
    public String urlConsulta(QrCodeNfce qrCode) {
        String host = qrCode.ambiente() == 2 ? HOST_HOMOLOGACAO : HOST_PRODUCAO;
        // Sem o parâmetro p (chave digitada), usa o formato online da versão 3 do QR code, que não exige o hash do CSC.
        String p = Optional.ofNullable(qrCode.parametroP())
                .orElse(qrCode.chave().valor() + "|3|" + qrCode.ambiente());
        return host + CAMINHO_QRCODE + "?p=" + URLEncoder.encode(p, StandardCharsets.UTF_8).replace("%7C", "|");
    }

    @Override
    public NotaLida parse(Document html, QrCodeNfce qrCode) {
        List<ItemLido> itens = lerItens(html);
        if (itens.isEmpty()) {
            throw erroSemItens(html);
        }

        String chaveNaPagina = somenteDigitos(texto(html.selectFirst(".chave")));
        if (!chaveNaPagina.isEmpty() && !chaveNaPagina.equals(qrCode.chave().valor())) {
            throw NfceException.naoEncontrada();
        }

        List<String> avisos = new ArrayList<>();
        MercadoLido mercado = lerMercado(html, qrCode.chave().cnpjEmitente());
        if (mercado.nome().isEmpty()) {
            avisos.add("Nome do mercado não encontrado.");
        }

        BigDecimal somaItens = itens.stream().map(ItemLido::precoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorTotal = total(html, "valor total").orElse(somaItens);
        BigDecimal descontos = total(html, "desconto").orElse(BigDecimal.ZERO);
        BigDecimal valorPago = total(html, "valor a pagar").orElse(valorTotal.subtract(descontos));

        if (somaItens.subtract(valorTotal).abs().compareTo(TOLERANCIA_TOTAL) > 0) {
            avisos.add("Soma dos itens (" + somaItens + ") difere do total da nota (" + valorTotal + ").");
        }

        return new NotaLida(
                qrCode.chave().valor(),
                uf(),
                mercado,
                lerDataEmissao(html),
                centavos(valorTotal),
                centavos(descontos),
                centavos(valorPago),
                itens,
                avisos);
    }

    private List<ItemLido> lerItens(Document html) {
        List<ItemLido> itens = new ArrayList<>();
        for (Element linha : html.select("#tabResult tr")) {
            String descricao = texto(linha.selectFirst("span.txtTit"));
            if (descricao.isEmpty()) {
                continue;
            }
            try {
                String codigo = somenteDigitos(texto(linha.selectFirst(".RCod")));
                String unidade = semRotulo(texto(linha.selectFirst(".RUN"))).toUpperCase(Locale.ROOT);
                itens.add(new ItemLido(
                        itens.size() + 1,
                        descricao,
                        codigo.isEmpty() ? null : codigo,
                        NumeroBr.gtinValido(codigo) ? NumeroBr.normalizarGtin(codigo) : null,
                        NumeroBr.parse(semRotulo(texto(linha.selectFirst(".Rqtd")))),
                        unidade.isEmpty() ? "UN" : unidade,
                        NumeroBr.parse(semRotulo(texto(linha.selectFirst(".RvlUnit")))),
                        centavos(NumeroBr.parse(texto(linha.selectFirst(".valor"))))));
            } catch (NumberFormatException exception) {
                throw NfceException.layoutDesconhecido();
            }
        }
        return itens;
    }

    private MercadoLido lerMercado(Document html, String cnpjDaChave) {
        String nome = Optional.ofNullable(html.selectFirst("#u20"))
                .or(() -> Optional.ofNullable(html.selectFirst(".txtTopo")))
                .map(NfceSpParser::texto)
                .orElse("");

        List<String> linhas = html.select("#conteudo .txtCenter .text").stream().map(NfceSpParser::texto).toList();
        String cnpj = linhas.stream()
                .filter(linha -> linha.toUpperCase(Locale.ROOT).contains("CNPJ"))
                .map(NfceSpParser::somenteDigitos)
                .filter(digitos -> digitos.length() == 14)
                .findFirst()
                .orElse(cnpjDaChave);
        String endereco = linhas.stream()
                .filter(linha -> !linha.toUpperCase(Locale.ROOT).contains("CNPJ"))
                .map(linha -> linha.replaceAll("(\\s*,\\s*)+", ", ").replaceAll("^,\\s*|,\\s*$", ""))
                .filter(linha -> !linha.isEmpty())
                .findFirst()
                .orElse(null);

        return new MercadoLido(cnpj, nome, endereco);
    }

    /** Procura no bloco de totais o valor cujo rótulo contém o trecho informado. */
    private Optional<BigDecimal> total(Document html, String trechoRotulo) {
        for (Element linha : html.select("#totalNota [id=linhaTotal]")) {
            String rotulo = texto(linha.selectFirst("label")).toLowerCase(Locale.ROOT);
            if (rotulo.contains(trechoRotulo)) {
                try {
                    return Optional.of(NumeroBr.parse(texto(linha.selectFirst(".totalNumb"))));
                } catch (NumberFormatException exception) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private LocalDateTime lerDataEmissao(Document html) {
        String infos = Optional.ofNullable(html.selectFirst("#infos")).map(Element::text).orElse(html.text());
        Matcher matcher = DATA_EMISSAO.matcher(infos);
        if (!matcher.find()) {
            throw NfceException.layoutDesconhecido();
        }
        return LocalDateTime.parse(matcher.group(1).replaceAll("\\s+", " "), FORMATO_DATA);
    }

    /** Diferencia "a Sefaz respondeu mas não achou a nota" de "o layout mudou". */
    private NfceException erroSemItens(Document html) {
        String corpo = html.text().toLowerCase(Locale.ROOT);
        if (corpo.contains("captcha")) {
            return NfceException.sefazIndisponivel();
        }
        if (corpo.matches("(?s).*(n[ãa]o (foi )?encontrad|inexistente|inv[áa]lid|n[ãa]o autorizad|rejei).*")) {
            return NfceException.naoEncontrada();
        }
        return NfceException.layoutDesconhecido();
    }

    private static String texto(Element elemento) {
        return elemento == null ? "" : elemento.text().replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    /** Remove o rótulo ("Qtde.:", "UN:", "Vl. Unit.:") e devolve só o valor. */
    private static String semRotulo(String valor) {
        int i = valor.indexOf(':');
        return (i >= 0 ? valor.substring(i + 1) : valor).trim();
    }

    private static String somenteDigitos(String texto) {
        return texto.replaceAll("\\D", "");
    }

    private static BigDecimal centavos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
