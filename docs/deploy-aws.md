# Deploy na AWS (ECS Fargate + RDS)

Terraform em [`infra/terraform/`](../infra/terraform). Ambiente único, foco em baixo custo e simplicidade. A aplicação fica em **https://jusbr.precohonesto.com**, com DNS no Cloudflare (zona `precohonesto.com`).

## Arquitetura

```mermaid
flowchart LR
  user((Usuário)) -->|jusbr.precohonesto.com| cf[Cloudflare DNS<br/>CNAME, DNS only]
  cf -.-> alb
  user -->|"HTTPS :443 (80 → 301)"| alb[ALB + ACM]
  subgraph VPC["VPC (2 AZs)"]
    subgraph pub[Subnets públicas]
      alb
      be[ECS Fargate<br/>backend :8080]
      fe[ECS Fargate<br/>frontend :3000]
    end
    subgraph db[Subnets de banco]
      rds[(RDS PostgreSQL 17<br/>db.t4g.micro)]
    end
  end
  alb -->|"/api/*, default (/{code})"| be
  alb -->|"/, /analytics/*, /_next/*, /favicon.ico"| fe
  fe -->|BACKEND_URL = https://domínio| alb
  be -->|5432| rds
  be -.->|secrets| sm[Secrets Manager<br/>senha gerenciada pelo RDS]
  ecr[ECR backend/frontend] -.->|pull| be & fe
```

| Arquivo | Conteúdo |
|---|---|
| `versions.tf` / `providers.tf` | Versões fixadas (AWS, Cloudflare), backend S3 (config parcial), `default_tags` |
| `network.tf` | VPC (módulo `terraform-aws-modules/vpc`), security groups |
| `ecr.tf` | Repositórios com scan on push e lifecycle (últimas N imagens) |
| `alb.tf` | ALB, target groups, listener HTTPS com regras por path, redirect HTTP→HTTPS |
| `dns.tf` | Certificado ACM, registros de validação e CNAME do app no Cloudflare |
| `ecs.tf` | Cluster, task definitions, services, logs, IAM de execução |
| `rds.tf` | PostgreSQL 17 com senha gerenciada no Secrets Manager |

Roteamento do ALB (listener 443): `/api/*` → backend; `/`, `/analytics/*`, `/_next/*`, `/favicon.ico` → frontend; qualquer outro path (short codes) → backend. O listener 80 só redireciona (301) para HTTPS.

Variáveis de ambiente:

- Backend: `DB_URL` (endpoint do RDS), `APP_BASE_URL` e `APP_FRONTEND_URL` = `https://jusbr.precohonesto.com`; `DB_USER`/`DB_PASSWORD` injetados via `secrets` do ECS a partir do secret gerenciado pelo RDS.
- Frontend: `BACKEND_URL` = `https://jusbr.precohonesto.com`. O fetch server-side sai pela internet e volta pelo ALB; é a opção mais simples (sem Service Connect/Cloud Map).

## Domínio e TLS

- **Certificado:** ACM para `jusbr.precohonesto.com`, validado por DNS. O Terraform cria o CNAME de validação no Cloudflare e aguarda a emissão (`aws_acm_certificate_validation`) antes de criar o listener 443 (política `ELBSecurityPolicy-TLS13-1-2-2021-06`).
- **Registro do app:** CNAME `jusbr` → DNS do ALB, `proxied = false` (DNS only), TTL 300.
- **Por que DNS only:** o TLS termina no ALB com ACM, sem depender do modo SSL da zona (em *Flexible* o Cloudflare falaria HTTP com a origem e o redirect HTTP→HTTPS causaria loop). Também mantém o IP real do cliente em `X-Forwarded-For`, usado no tracking de cliques.
- **Token Cloudflare:** API token com permissões **Zone → Zone → Read** e **Zone → DNS → Edit**, restrito à zona `precohonesto.com`. O provider lê o token da variável de ambiente `CLOUDFLARE_API_TOKEN`; nunca coloque o token em arquivos `.tf`/`.tfvars`.

## Tags de custo

Todos os recursos AWS recebem `Project = jusbr` e `ManagedBy = terraform` via `default_tags` (`project_name`, que também prefixa os nomes). O bucket de state, criado fora do Terraform, é tagueado manualmente (passo 1). Ative a tag `Project` em *Billing → Cost allocation tags* para filtrar no Cost Explorer.

## Decisões e trade-offs (custo)

- **Sem NAT Gateway:** tasks Fargate ficam em subnets públicas com IP público (necessário para puxar imagens do ECR e ler o Secrets Manager). A entrada é restrita por security group (só o ALB alcança as portas das tasks). Economiza ~US$ 32/mês por NAT.
- **RDS single-AZ, db.t4g.micro:** sem failover automático. Criptografado, sem acesso público, porta 5432 aberta apenas ao SG do backend. Backups de 7 dias.
- **Proteções do banco:** `db_deletion_protection = true` e `db_skip_final_snapshot = false` por padrão. Para destruir, altere as duas variáveis (ver abaixo).
- **ARM64 (Graviton):** Fargate ARM é mais barato e builda nativamente em Macs Apple Silicon. Todas as imagens devem ser `linux/arm64`.
- **1 task por serviço**, sem autoscaling. Circuit breaker com rollback habilitado.
- **ECR com `force_delete`:** imagens são reconstruíveis; permite `destroy` sem esvaziar o repositório.
- **Health check do frontend em `/`:** a página consulta o backend, então o frontend só fica saudável com o backend no ar.

## Pré-requisitos

- Terraform `~> 1.10`, AWS CLI v2 com o profile `default` configurado, Docker com `buildx`.
- `.env` na raiz do repositório (gitignored) com `CLOUDFLARE_API_TOKEN` (e `CLOUDFLARE_ACCOUNT_ID`).

## Passo a passo

```bash
cd infra/terraform
export AWS_PROFILE=default AWS_REGION=us-east-1
set -a; source ../../.env; set +a      # exporta CLOUDFLARE_API_TOKEN sem exibi-lo
```

1. **Bucket de state** (uma vez; nome globalmente único):

   ```bash
   BUCKET=<seu-prefixo>-jusbr-tfstate
   aws s3api create-bucket --bucket "$BUCKET"
   aws s3api put-bucket-versioning --bucket "$BUCKET" --versioning-configuration Status=Enabled
   aws s3api put-bucket-tagging --bucket "$BUCKET" --tagging 'TagSet=[{Key=Project,Value=jusbr},{Key=ManagedBy,Value=manual}]'
   ```

2. **Configuração:**

   ```bash
   cp backend.hcl.example backend.hcl          # ajuste bucket
   cp terraform.tfvars.example terraform.tfvars
   terraform init -backend-config=backend.hcl
   ```

3. **ECR primeiro** (as services precisam de imagens para subir):

   ```bash
   terraform apply -target=aws_ecr_repository.app
   ```

4. **Build e push das imagens (linux/arm64):**

   ```bash
   ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
   REGISTRY=$ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com
   aws ecr get-login-password | docker login --username AWS --password-stdin "$REGISTRY"

   for app in backend frontend; do
     docker buildx build --platform linux/arm64 \
       -t "${REGISTRY}/jusbr/${app}:latest" --push "../../${app}"
   done
   ```

5. **Apply completo:**

   ```bash
   terraform apply
   ```

   O primeiro apply leva ~10 min (RDS e emissão do certificado). O backend roda as migrations do Flyway ao subir.

6. **Smoke test:**

   ```bash
   URL=$(terraform output -raw app_url)     # https://jusbr.precohonesto.com
   curl -sI http://jusbr.precohonesto.com/ | head -3   # 301 para https://
   curl -s -X POST "$URL/api/shorten" -H 'Content-Type: application/json' -d '{"url":"https://example.com"}'
   curl -i "$URL/<shortCode>"            # 301 para https://example.com
   curl -s "$URL/api/stats/<shortCode>"
   open "$URL"                           # lista; /analytics/<shortCode> para analytics
   ```

## Atualizar uma versão

Com tag fixa (`latest`), faça push da nova imagem e force um novo deploy:

```bash
aws ecs update-service --cluster jusbr --service backend --force-new-deployment
```

Alternativa: publicar com uma tag nova (ex.: SHA do commit) e rodar `terraform apply -var image_tag=<sha>`.

Logs: CloudWatch em `/ecs/jusbr/backend` e `/ecs/jusbr/frontend`.

## Destroy

```bash
terraform apply -var db_deletion_protection=false -var db_skip_final_snapshot=true
terraform destroy -var db_deletion_protection=false -var db_skip_final_snapshot=true
```

Mantendo `db_skip_final_snapshot = false`, o RDS gera o snapshot `jusbr-final` (remova-o manualmente se não precisar). O destroy também remove os registros DNS no Cloudflare. O bucket de state não é gerenciado por este Terraform.

## Próximos passos

- **CI/CD:** GitHub Actions com OIDC (role IAM sem chaves estáticas) para build/push no ECR e deploy com tag por commit.
- **Autoscaling:** Application Auto Scaling nas services (CPU/requests por target); RDS Multi-AZ quando houver requisito de disponibilidade.
- **Rede privada:** tasks em subnets privadas com NAT ou VPC endpoints, e `BACKEND_URL` via Service Connect em vez do domínio público.
