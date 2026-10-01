# CLAUDE.md · minhacestinha api

## Stack

Java 21 · Spring Boot 4 · PostgreSQL 16 · Flyway · JWT (Auth0) · Jsoup · springdoc · Lombok.

## Padrões do projeto

**Controller: interface + implementação**
- `controller/FooController.java`: interface com `@RequestMapping`, mapeamentos e anotações do Swagger.
- `controller/impl/FooControllerImpl.java`: `@RestController @RequiredArgsConstructor`, só delega pro serviço.
- Usuário logado: `@Parameter(hidden = true) @AuthenticationPrincipal User usuario`.

**Serviço: interface + implementação**
- `service/<dominio>/FooService.java` + `FooServiceImpl.java` (`@Service @RequiredArgsConstructor`).
- Leitura com `@Transactional(readOnly = true)`, escrita com `@Transactional`.
- Chamada externa lenta (Sefaz, Cosmos, Claude, Banco Central) fica **fora** da transação (ver `NotaServiceImpl.importarPorQrCode`).
- Integração externa opcional falha em silêncio (loga e segue): sem chave ou fora do ar, o fluxo principal continua.

**DTOs são records** em `dto/request`, `dto/response` e `dto/auth`, com Bean Validation e `@Schema`.

**Entidades** herdam `AuditoriaBase` (`data_inclusao`, `data_alteracao`), usam Lombok `@Getter @Setter @Builder`
e `@EqualsAndHashCode(of = "id")`. Nunca `@Data` em entidade.

**Injeção sempre por construtor** (`@RequiredArgsConstructor`).

**Mensagens** de erro em `messages.properties`, acessadas por `Mensagens`.

**Erros**: `ResponseStatusException` com a mensagem, ou `NfceException` na leitura da nota.
O `GlobalExceptionHandler` formata tudo em `ErrorResponse`. Erro inesperado vira 500 sem detalhes (é logado).

**Datas**: use o `Clock` injetado (fuso America/Sao_Paulo), nunca `LocalDate.now()` direto em regra de negócio.

## Regras

1. **Migrations**: nunca altere uma migration já existente. Crie `V{n+1}__descricao.sql`.
2. **Soft delete**: registros com `ativo` nunca são apagados, só `ativo = false`. Única exceção: `DELETE /api/users/me` (LGPD) apaga tudo do usuário de verdade.
3. **Nunca retorne entidade JPA** no controller. Converta para DTO (mappers em `persistence/mapper`).
4. **Todo dado é do usuário logado**: sempre filtre por `usuario.getId()` nas consultas.
5. **Endpoint novo público** precisa entrar em `SecurityConfig` (o padrão é exigir autenticação).
6. **Nunca use a URL do QR code para buscar a nota**: o parser monta a URL oficial a partir da chave (evita SSRF).
7. **Novo estado**: `service/nfce/NfceXxParser implements NfceParser`, com HTML de exemplo em
   `src/test/resources/nfce/` e teste unitário do parser.
8. **Testes**: unitários terminam em `UnitTest` (surefire), integração em `TestIntegration` (failsafe, H2 no modo PostgreSQL).

## Como rodar

```bash
docker compose up --build        # API + Postgres
./mvnw spring-boot:run           # só a API (precisa das variáveis do .env.example)
./mvnw verify                    # todos os testes
```
