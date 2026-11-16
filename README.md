# URL Shortener

Encurtador de URLs com tracking de cliques e página de analytics. Enunciado em [docs/challenge.md](docs/challenge.md).

## Stack e justificativa

- **Backend:** Kotlin + Spring Boot 4 (Gradle Kotlin DSL, JDK 17). Stack que domino, tipagem forte, ecossistema maduro para REST, validação e testes.
- **Persistência:** PostgreSQL + Spring Data JPA + Flyway. Agregações de analytics (cliques por dia, top referrers) são naturais em SQL; schema versionado.
- **Frontend:** Next.js (App Router) + TypeScript + Tailwind + shadcn/ui para a página de analytics.
- **Testes:** JUnit 5, MockK (unidade) e Testcontainers (integração com Postgres real).

## Pré-requisitos

- JDK 17+ (`JAVA_HOME` apontando para ele), Docker, Node 20+.

## Rodando (dev)

```bash
docker compose up -d postgres
cd backend && ./gradlew bootRun
```

## Testes

```bash
cd backend && ./gradlew test
```
