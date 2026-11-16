## Princípios

Seguir estritamente YAGNI, KISS e DRY; tanto no código quanto na comunicação.

- **YAGNI:** implementar apenas o necessário para os requisitos atuais. Não criar abstrações, configurações ou generalizações especulativas.
- **KISS:** escolher a solução mais simples que resolva o problema com clareza e segurança.
- **SOLID:** aplicar de forma pragmática: responsabilidade única, extensão sem alterações desnecessárias, substituibilidade, interfaces pequenas e dependência de abstrações nas fronteiras que justificarem isso. Não criar interfaces ou classes apenas para demonstrar padrões.
- **DRY com critério:** eliminar duplicação de conhecimento e regras, sem forçar reutilização de código que tem motivos distintos para mudar.
- **Coesão alta e acoplamento baixo:** manter regras de negócio isoladas de HTTP, UI, banco e provedores externos quando houver benefício concreto.
- **Código:** implementar só o que o problema atual exige. Escolher a solução mais simples que funcione. 
- **TDD:** para regras de negócio, começar por um teste que falha, implementar o mínimo para passar e refatorar mantendo os testes verdes.
- **Legibilidade:** nomes expressivos, funções pequenas e comportamento explícito. Evitar comentários que apenas repetem o código.
- **Comunicação:** respostas curtas e diretas, com decisões, premissas, riscos e validações relevantes. Não apresentar alternativas ou explicações extensas sem necessidade.
- **Idioma:** conversar e explicar decisões em português. Escrever código, identificadores, comentários de código, nomes de arquivos técnicos e mensagens de commit em inglês. A documentação explicativa pode ser em português; contratos e nomes técnicos permanecem em inglês.
- **Design:** baixo acoplamento, alta coesão e software sempre dividido em camadas (domínio, controller, service, repository, config).
- **Docs:** manter documentação técnica em `docs/`, com exceção de `README.md` e `CLAUDE.md` na raiz. Atualizar documentos existentes em vez de criar duplicatas.

## Testes

- **Unidade:** obrigatório para todo código que envolve regra de negócio. Buscar uma cobertura de teste acima de 85% quando for possível.
- **Integração:** obrigatório para toda API.
- **End-to-end:** ao fechar uma feature, atualizar a collection do Postman, se essa não existir, crie.

## Stack padrão
- **Backend:** Kotlin, Spring Boot, Gradle Kotlin DSL, REST e JSON.
- **Frontend:** Next.js (App Router), TypeScript, Tailwind CSS e shadcn/ui.
- **Persistência:** PostgreSQL com Spring Data JPA quando adequado; avaliar alternativas apenas diante de requisitos concretos.
- **Testes backend:** JUnit 5, MockK e Testcontainers para integração real com serviços externos ou banco.
- **Testes frontend:** Vitest, React Testing Library e Playwright para fluxos críticos de ponta a ponta quando necessário.
- **Execução local:** Docker e Docker Compose.
- **Infraestrutura:** AWS provisionada e versionada com Terraform.
- **Versões:** preferir versões estáveis, compatíveis e fixadas; seguir as versões existentes no repositório antes de introduzir atualizações.

## Estrutura do repositório
```text
project/
├── backend/
│   ├── src/main/kotlin/
│   ├── src/main/resources/
│   └── src/test/kotlin/
├── frontend/
│   ├── src/app/
│   ├── src/components/
│   └── src/lib/
├── infra/
│   └── terraform/
├── docs/
├── docker-compose.yml
├── .env.example
├── README.md
└── CLAUDE.md
```