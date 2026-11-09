# siase-execution-service

> SIASE — Fase 4 | Microsserviço de Execução de Serviços

## Descrição

Responsável por gerenciar a fila de execução das OS aprovadas, controlar o ciclo de execução pelos mecânicos e publicar eventos de conclusão via **AWS SQS**. Usa **DynamoDB** para a fila de estado em tempo real e **PostgreSQL** para histórico de execuções.

## Responsabilidades

- Consumir `ORCAMENTO_APROVADO` → enfileirar OS para execução
- Mecânico inicia execução → publica `EXECUCAO_INICIADA`
- Mecânico finaliza execução → publica `EXECUCAO_FINALIZADA`
- Compensação: cancelar execução ao receber `OS_CANCELADA`

## Bancos de Dados

| Banco | Uso |
|---|---|
| DynamoDB `siase-execution-fila` | Estado em tempo real da fila (NA_FILA, EM_EXECUCAO, FINALIZADA) |
| PostgreSQL `siase_exec_db` | Histórico de execuções para auditoria e métricas |

## Endpoints

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/execucao/fila` | JWT | Listar fila |
| GET | `/api/execucao/fila/{osId}` | JWT | Consultar OS na fila |
| PATCH | `/api/execucao/fila/{osId}/iniciar` | JWT | Iniciar execução |
| PATCH | `/api/execucao/fila/{osId}/finalizar` | JWT | Finalizar execução |

## Testes

```bash
mvn clean verify
open target/site/jacoco/index.html
open target/cucumber-reports/report.html
```

## Documentação

- [ADR-001 — DynamoDB para Fila de Execução](docs/adr/ADR-001-dynamodb-fila.md)
