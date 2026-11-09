# ADR-002 — PostgreSQL para Histórico de Execuções

**Status:** Aceito  
**Data:** 2025-01

## Contexto

O Execution Service usa DynamoDB para o estado em tempo real da fila. Porém, o desafio exige uso de pelo menos um banco relacional (SQL) por serviço. Além disso, há necessidade de queries analíticas sobre execuções históricas (tempo médio por tipo de serviço, produtividade por mecânico).

## Decisão

Usar **PostgreSQL** (RDS `siase_exec_db`) para a tabela `itens_execucao`, que registra cada evento de início e fim de execução com o mecânico responsável.

## Divisão de responsabilidades

| Banco | Tabela/Recurso | Uso |
|---|---|---|
| DynamoDB `siase-execution-fila` | Estado atual da fila | Leitura/escrita em tempo real pelo mecânico |
| PostgreSQL `itens_execucao` | Histórico de execuções | Auditoria, métricas, relatórios |

## Consequências

- **Positivo**: cumpre requisito de SQL + NoSQL por serviço
- **Positivo**: queries analíticas no PostgreSQL sem impactar a fila em tempo real
- **Negativo**: dois bancos para gerenciar — mitigado pelo RDS gerenciado e Flyway
