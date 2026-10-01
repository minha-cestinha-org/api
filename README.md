# minhacestinha · api

API da **minhacestinha**: lê a nota fiscal do mercado (NFC-e) pelo QR code, guarda o histórico de preços de cada produto e mostra quanto você gastou.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Security, Validation, Actuator) |
| Banco | PostgreSQL 16 + Flyway |
| Autenticação | JWT (Auth0 java-jwt), access token de 1h e refresh de 30 dias |
| Leitura da NFC-e | Jsoup (scraping do portal da Sefaz) |
| Nomes dos produtos | Cosmos (Bluesoft) por EAN + Claude (anthropic-java) |
| IPCA | API SGS do Banco Central (série 433) |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, MockMvc, H2 (modo PostgreSQL), JaCoCo |

## Como rodar

### Com Docker (API + Postgres)

```bash
docker compose up --build
```

### Só a API (com um Postgres local)

```bash
cp .env.example .env   # ajuste os valores
export $(cat .env | xargs)
./mvnw spring-boot:run
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Testes

```bash
./mvnw verify   # unitários (*UnitTest) + integração (*TestIntegration)
```

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| POST | `/api/auth/register` | cria conta (aceite dos termos obrigatório) |
| POST | `/api/auth/login` | login, devolve `token` e `refreshToken` |
| POST | `/api/auth/refresh` | renova os tokens |
| GET | `/api/users/me` | dados da conta |
| GET | `/api/users/me/dados` | baixar todos os meus dados (LGPD) |
| DELETE | `/api/users/me` | apagar a conta e tudo do usuário |
| POST | `/api/notas/qrcode` | importa a nota a partir do QR code (ou da chave de acesso) |
| GET | `/api/notas` | lista as compras |
| GET | `/api/notas/{id}` | nota com os itens |
| PATCH | `/api/notas/{id}/desativar` | remove a nota do histórico (soft delete) |
| GET | `/api/produtos` | preços: último, anterior, variação, menor e média (os cards) |
| GET | `/api/produtos/{id}/historico` | todas as compras de um produto |
| GET | `/api/produtos/ean/{ean}` | modo mercado: histórico pelo código de barras |
| PUT | `/api/produtos/{id}` | corrige nome, marca e categoria (só pro usuário) |
| GET | `/api/gastos/mensal?meses=6` | gasto por mês |
| GET | `/api/gastos/inflacao?meses=12` | inflação pessoal vs IPCA |

## Como a leitura da nota funciona

1. O app manda o conteúdo do QR code (`QrCodeNfce` extrai e valida a chave de acesso de 44 dígitos).
2. O parser do estado monta a **URL oficial** da Sefaz a partir da chave (nunca usa o host que veio no QR code).
3. `SefazClient` baixa a página e o parser (`NfceSpParser`) lê mercado, itens, totais e data.
4. Cada item vira um produto: primeiro pelo cache "descrição + mercado", depois pelo EAN, senão cria um novo.
5. Produto novo ganha nome padronizado: Cosmos pelo EAN e, se não achar, Claude (`LTE COND INTEG ITAMB 1L` → `leite integral itambé 1l`).
   Sem `ANTHROPIC_API_KEY`/`COSMOS_TOKEN`, ou se a chamada falhar, fica a descrição em minúsculo.
   Sefaz, Cosmos e Claude rodam **fora** da transação.

## Variáveis de ambiente

| Variável | Obrigatória | Para quê |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | sim | Postgres |
| `JWT_SECRET` | sim | assinatura dos tokens |
| `CORS_ALLOWED_ORIGINS` | não | origens do app |
| `ANTHROPIC_API_KEY` | não | padronização de nomes com Claude |
| `IA_MODELO` | não | modelo do Claude (padrão `claude-opus-5-5`) |
| `COSMOS_TOKEN` | não | catálogo de produtos por EAN |

Por enquanto só **SP**. Para outro estado: criar `NfceXxParser implements NfceParser` e ele é registrado sozinho.

## Estrutura

```
src/main/java/minhacestinha/api/
├── config/          SecurityConfig, SecurityFilter, OpenApiConfig, ClockConfig
├── controller/      interfaces com as anotações do Swagger
│   └── impl/        implementações (@RestController)
├── dto/             auth/, request/, response/ (records)
├── exception/       GlobalExceptionHandler, ErrorResponse
├── message/         Mensagens (messages.properties)
├── persistence/     entity/, repository/, mapper/
└── service/         nfce/, nota/, produto/, padronizacao/, gasto/, user/, security/
```

As regras de código do projeto estão no [CLAUDE.md](CLAUDE.md).
