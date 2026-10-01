package minhacestinha.api.service.padronizacao;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Padroniza descrições de nota fiscal com o Claude ("LTE COND INTEG ITAMB 1L" → "leite integral itambé 1L").
 * Um pedido por nota, com todos os itens novos. Sem {@code ANTHROPIC_API_KEY}, não faz nada.
 */
@Slf4j
@Component
public class ClaudePadronizador {

    static final List<String> CATEGORIAS = List.of(
            "hortifruti", "açougue e peixaria", "frios e laticínios", "padaria", "mercearia", "bebidas",
            "bebidas alcoólicas", "congelados", "limpeza", "higiene e beleza", "bebê", "pet", "utilidades", "outros");

    private static final String INSTRUCOES = """
            Você padroniza descrições de produtos que vêm abreviadas em notas fiscais de supermercados do Brasil.
            Para cada descrição, devolva:
            - nome: nome legível em português, em letras minúsculas, com tipo, marca e tamanho quando houver.
              Mantenha unidades como na embalagem (1L, 500g, 2kg). Ex.: "LTE COND INTEG ITAMB 1L" vira "leite integral itambé 1L".
            - marca: só a marca, em minúsculas, ou null quando for produto a granel ou sem marca.
            - categoria: exatamente uma destas: %s.
            Se não der para entender a descrição, use a própria descrição em minúsculas como nome e a categoria "outros".
            Devolva um item para cada descrição recebida, com a descricaoBruta exatamente igual à recebida.
            """.formatted(String.join(", ", CATEGORIAS));

    private final AnthropicClient client;
    private final String modelo;

    public ClaudePadronizador(@Value("${ia.api-key:}") String apiKey,
                              @Value("${ia.modelo:claude-opus-5-5}") String modelo) {
        this.client = apiKey == null || apiKey.isBlank() ? null : AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.modelo = modelo;
    }

    public boolean habilitado() {
        return client != null;
    }

    /** Devolve a padronização de cada descrição, indexada pela descrição bruta. Em caso de falha, devolve vazio. */
    public Map<String, ProdutoPadronizado> padronizar(List<String> descricoes, String nomeMercado) {
        if (!habilitado() || descricoes.isEmpty()) {
            return Map.of();
        }

        StructuredMessageCreateParams<Resposta> params = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(16000L)
                .system(INSTRUCOES)
                .outputConfig(Resposta.class)
                .addUserMessage("Mercado: " + nomeMercado + "\nDescrições:\n" + String.join("\n", descricoes))
                .build();

        try {
            return client.messages().create(params).content().stream()
                    .flatMap(bloco -> bloco.text().stream())
                    .flatMap(texto -> texto.text().itens().stream())
                    .filter(item -> descricoes.contains(item.descricaoBruta()))
                    .collect(Collectors.toMap(Item::descricaoBruta, ClaudePadronizador::paraProduto,
                            (primeiro, segundo) -> primeiro));
        } catch (AnthropicException | IllegalStateException exception) {
            log.warn("Falha ao padronizar {} itens com o Claude: {}", descricoes.size(), exception.getMessage());
            return Map.of();
        }
    }

    private static ProdutoPadronizado paraProduto(Item item) {
        String categoria = CATEGORIAS.contains(item.categoria()) ? item.categoria() : "outros";
        return new ProdutoPadronizado(item.nome(), item.marca(), categoria);
    }

    record Resposta(List<Item> itens) {
    }

    record Item(
            @JsonPropertyDescription("Descrição exatamente como recebida") String descricaoBruta,
            @JsonPropertyDescription("Nome legível em minúsculas") String nome,
            @JsonPropertyDescription("Marca em minúsculas, ou null") String marca,
            @JsonPropertyDescription("Uma das categorias permitidas") String categoria) {
    }
}
