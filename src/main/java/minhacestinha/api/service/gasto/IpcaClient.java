package minhacestinha.api.service.gasto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/** IPCA mensal do Banco Central (SGS, série 433). Se a API não responder, devolve vazio. */
@Slf4j
@Component
public class IpcaClient {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;

    public IpcaClient(@Value("${ipca.url:https://api.bcb.gov.br}") String url) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(requestFactory).build();
    }

    /** IPCA acumulado (em %) entre os meses informados, inclusive. */
    public Optional<BigDecimal> acumulado(YearMonth inicio, YearMonth fim) {
        try {
            List<Ponto> pontos = restClient.get()
                    .uri("/dados/serie/bcdata.sgs.433/dados?formato=json&dataInicial={inicio}&dataFinal={fim}",
                            inicio.atDay(1).format(FORMATO), fim.atEndOfMonth().format(FORMATO))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Ponto>>() {
                    });
            if (pontos == null || pontos.isEmpty()) {
                return Optional.empty();
            }
            BigDecimal fator = pontos.stream()
                    .map(ponto -> BigDecimal.ONE.add(new BigDecimal(ponto.valor()).movePointLeft(2)))
                    .reduce(BigDecimal.ONE, (a, b) -> a.multiply(b, MathContext.DECIMAL64));
            return Optional.of(fator.subtract(BigDecimal.ONE).movePointRight(2).setScale(2, RoundingMode.HALF_UP));
        } catch (RestClientException | NumberFormatException exception) {
            log.warn("Não consegui buscar o IPCA: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Ponto(String data, String valor) {
    }
}
