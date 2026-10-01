package minhacestinha.api.service.padronizacao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * Catálogo Cosmos (Bluesoft): nome e marca do produto pelo código de barras.
 * Sem token configurado ({@code COSMOS_TOKEN}), não consulta nada.
 */
@Slf4j
@Component
public class CosmosClient {

    private final RestClient restClient;
    private final String token;

    public CosmosClient(@Value("${cosmos.url:https://api.cosmos.bluesoft.com.br}") String url,
                        @Value("${cosmos.token:}") String token) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(requestFactory).build();
        this.token = token;
    }

    public boolean habilitado() {
        return token != null && !token.isBlank();
    }

    public Optional<ProdutoPadronizado> buscarPorEan(String ean) {
        if (!habilitado() || ean == null) {
            return Optional.empty();
        }
        try {
            RespostaCosmos resposta = restClient.get()
                    .uri("/gtins/{ean}.json", ean)
                    .header("X-Cosmos-Token", token)
                    .header("User-Agent", "Cosmos-API-Request")
                    .retrieve()
                    .body(RespostaCosmos.class);
            if (resposta == null || resposta.description() == null || resposta.description().isBlank()) {
                return Optional.empty();
            }
            String marca = resposta.brand() == null ? null : minusculo(resposta.brand().name());
            return Optional.of(new ProdutoPadronizado(minusculo(resposta.description()), marca, null));
        } catch (RestClientException exception) {
            log.warn("Cosmos não respondeu para o EAN {}: {}", ean, exception.getMessage());
            return Optional.empty();
        }
    }

    private static String minusculo(String texto) {
        return texto == null ? null : texto.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaCosmos(String description, Marca brand) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Marca(String name) {
    }
}
