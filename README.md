# URL Shortener

Encurtador de URLs com tracking de cliques e página de analytics. Enunciado em [docs/challenge.md](docs/challenge.md).

## Rodando

Pré-requisito: Docker.

```bash
make run          # ou: docker compose up --build
```

- API: http://localhost:8080
- Frontend: http://localhost:3000 (lista de links) e http://localhost:3000/analytics/{code}

Se a porta 5432 já estiver em uso, rode com `DB_PORT=5433 make run`. Variáveis em [.env.example](.env.example).

### Modo dev (só o Postgres no Docker)

```bash
DB_PORT=5433 docker compose up -d postgres
cd backend && DB_URL=jdbc:postgresql://localhost:5433/shortener ./gradlew bootRun
cd frontend && npm run dev
```

## Testes

Pré-requisitos: JDK 17+ e Node 20+. Docker rodando (Testcontainers). No macOS o `Makefile` resolve o JDK 17 via `/usr/libexec/java_home`; em outros sistemas, exporte `JAVA_HOME`.

CI: GitHub Actions ([.github/workflows/ci.yml](.github/workflows/ci.yml)) roda testes do backend, lint/testes/build do frontend e o build das imagens Docker.

```bash
make test         # backend (JUnit + MockK + Testcontainers) e frontend (Vitest)
```

Testes end-to-end da API (stack no ar): `make e2e` roda a collection [docs/postman/url-shortener.postman_collection.json](docs/postman/url-shortener.postman_collection.json) com newman. Também pode ser importada no Postman.

## Demo rápido

```bash
curl -s -X POST localhost:8080/api/shorten -H 'Content-Type: application/json' -d '{"url":"https://example.com"}'
curl -i localhost:8080/{code} -H 'Referer: https://google.com'   # repita algumas vezes
open http://localhost:3000/analytics/{code}
```

## API

| Método | Rota | Resposta |
|---|---|---|
| POST | `/api/shorten` `{url, expiresAt?}` | 201 `{shortCode, shortUrl, originalUrl, createdAt, expiresAt, analyticsUrl}`; 400 se URL inválida ou `expiresAt` no passado |
| GET | `/{code}` | 301 + `Cache-Control: no-store`; 404 inexistente; 410 expirado. Registra clique (referrer, user-agent, ip, timestamp) |
| GET | `/api/urls` | Links mais recentes primeiro, com `clickCount` |
| GET | `/api/stats/{code}` | `totalClicks`, `clicksByDay` (30 dias, UTC, zero-fill), `topReferrers` (top 10) |

Erros retornam `{ "error": "..." }`. Body malformado (ex.: data inválida) retorna 400 `Malformed request body`. Em `topReferrers`, empates são ordenados alfabeticamente; referrer ausente aparece como `direct`.

## Stack e justificativa

- **Backend:** Kotlin + Spring Boot 4 (JDK 17). Ecossistema maduro para REST, validação e testes; tipagem forte.
- **Persistência:** PostgreSQL + Spring Data JPA + Flyway. Analytics são agregações (`GROUP BY` por dia/referrer), naturais em SQL; schema versionado. Postgres em vez de SQLite/memória por ser próximo de produção e suportar concorrência real de cliques.
- **Frontend:** Next.js (App Router) + Tailwind + shadcn/ui. Páginas são Server Components que chamam a API no servidor (`BACKEND_URL`), então o link de analytics é uma URL simples e compartilhável.
- **Testes:** JUnit 5 + MockK (unidade), Testcontainers (integração com Postgres real), Vitest + RTL (frontend).

## Decisões e trade-offs

- **301 + `Cache-Control: no-store`:** respeita o contrato pedido, mas impede o browser de cachear o redirect; sem isso, cliques repetidos não chegariam ao servidor e o tracking ficaria subcontado.
- **Short code:** 7 caracteres base62 com `SecureRandom` (~3,5 trilhões de combinações); unique constraint no banco + retry em colisão.
- **`analyticsUrl` na resposta do shorten:** extensão do contrato para facilitar compartilhar a página com quem criou o link.
- **`expiresAt` no passado → 400:** criar um link já expirado não tem uso legítimo.
- **Clique gravado de forma síncrona:** simples e consistente. Em escala, o próximo passo seria enfileirar (ex.: SQS) e agregar de forma assíncrona.
- **IP:** primeiro valor de `X-Forwarded-For`, senão o endereço remoto.
- **Sem autenticação:** fora do escopo; qualquer pessoa com o código vê o analytics.

## Deploy na AWS

Terraform em [infra/terraform/](infra/terraform/) (ECS Fargate + ALB + RDS PostgreSQL + ECR, DNS no Cloudflare). Passo a passo em [docs/deploy-aws.md](docs/deploy-aws.md).
