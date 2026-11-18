# siase-execution-service

Microsserviço de **execução de serviços** do sistema SIASE (oficina mecânica) —
Fase 4 do Tech Challenge FIAP. Gerencia a fila de execução das Ordens de Serviço
aprovadas e publica os eventos do Saga quando o serviço é iniciado/finalizado.

## Arquitetura

Hexagonal (ports & adapters), Java 17 + Spring Boot 3.2.4:

```
┌─────────── REST /api/execucao ──────────┐      ┌─ DynamoDB: fila (NoSQL)
│  GET    /api/execucao/fila              │ ───▶ │
│  GET    /api/execucao                   │      └─ Postgres: histórico (SQL)
│  POST   /api/execucao/{osId}/iniciar    │
│  POST   /api/execucao/{osId}/finalizar  │
└─────────────────────────────────────────┘
   SQS execution-queue (in): ORCAMENTO_APROVADO → enfileira
                            OS_CANCELADA        → compensação
   SQS os-updates-queue (out): EXECUCAO_INICIADA / EXECUCAO_FINALIZADA / EXECUCAO_CANCELADA
```

## Saga Pattern — papel do serviço

Coreografia via SQS (decisão e diagrama completos em `docs/adr/ADR-001`):

- **Consome** `siase-execution-queue`: `ORCAMENTO_APROVADO` (enfileira a OS),
  `OS_CANCELADA` (**compensação**: cancela o item e publica `EXECUCAO_CANCELADA`)
- **Produz** para `siase-os-updates-queue`: `EXECUCAO_INICIADA` (os-service →
  `EM_EXECUCAO`) e `EXECUCAO_FINALIZADA` (os-service → `FINALIZADA` /
  `AGUARDANDO_PAGAMENTO`)
- Idempotência: `ORCAMENTO_APROVADO` duplicado é ignorado (chave `osId`)

## Bancos de dados (por serviço)

| Dado | Banco | Por quê |
|---|---|---|
| Fila de execução | DynamoDB `siase-<env>-execution-fila` | key-value por `osId`, alta vazão (NoSQL) |
| Histórico de execução | Postgres `siase_exec_db` | trilha de auditoria relacional (SQL) |

Nenhum serviço acessa o banco de outro — atualização de estado só via eventos.

## Testes

- Unitários (JUnit 5) cobrindo os 5 use cases + domínio
- **BDD (Cucumber)** `src/test/resources/features/execution_saga.feature`:
  fluxo feliz de execução + compensação por cancelamento
- **JaCoCo** com gate de 80% de linhas no `mvn verify`

```bash
mvn clean verify
```

## CI/CD

`.github/workflows/ci.yml`: testes+cobertura → SonarCloud → build/push da imagem
(GHCR) → deploy EKS homolog → deploy prod com aprovação manual.
Branch protection na `main` (PR obrigatório + checks) 

## Observabilidade (continuação da Fase 3)

- `/actuator/prometheus` (Micrometer) — scrape via annotations no Deployment
- Métrica do Saga: `siase_saga_eventos_total{servico="execution",evento,direcao}`
- Logs JSON (logstash-encoder) com `correlationId` — propagado por header HTTP
  `X-Correlation-Id` e **message attribute** do SQS (rastreio ponta a ponta)
- Dashboard Grafana importável no pacote `siase-fase4`

## API

Swagger UI: `/api/swagger-ui.html` · OpenAPI: `/api/v3/api-docs`

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/execucao/fila` | OSs aguardando execução |
| GET | `/api/execucao` | todos os itens + histórico |
| POST | `/api/execucao/{osId}/iniciar` | inicia execução (mecânico) |
| POST | `/api/execucao/{osId}/finalizar` | finaliza execução |

Endpoints internos exigem JWT (HS256, mesmo `JWT_SECRET` da auth-lambda da Fase 3).

## Deploy

```bash
docker build -t ghcr.io/siase-soat/siase-execution-service:tag .
kubectl apply -f k8s/deployment.yaml   # ajustar imagem/envs
```

Env vars: `DB_HOST/DB_NAME/DB_USER/DB_PASSWORD`, `SQS_EXECUTION_URL`,
`SQS_OS_UPDATES_URL`, `DYNAMODB_FILA_TABLE`, `JWT_SECRET`, `AWS_REGION`.

Porta: **8083**

## Documentação

- [ADR-001 — DynamoDB para Fila de Execução em Tempo Real](docs/adr/ADR-001-dynamodb-fila.md)
- [ADR-002 — PostgreSQL para Histórico de Execuções](docs/adr/ADR-002-postgresql-historico.md)
- [RFC-001 — Contratos de Eventos SQS do Execution Service](docs/rfc/RFC-001-contratos-eventos-execution.md)
- [Diagramas de Sequência](docs/diagramas-sequencia.md)
