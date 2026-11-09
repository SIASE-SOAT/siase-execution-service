# ADR-001 — DynamoDB para Fila de Execução em Tempo Real

**Status:** Aceito  
**Data:** 2025-01

## Decisão

Usar **DynamoDB** para armazenar o estado da fila de execução em tempo real, com PostgreSQL para histórico.

## Justificativa

A fila de execução tem padrão de acesso por chave primária (`osId`), com atualizações frequentes de status. DynamoDB é ideal para este padrão: acesso O(1) por chave, sem necessidade de queries complexas.

O histórico de execuções (tabela `itens_execucao` no PostgreSQL) permite queries analíticas como tempo médio de execução por tipo de serviço.

## Tabela DynamoDB `siase-execution-fila`

| Atributo | Tipo | Descrição |
|---|---|---|
| `osId` (PK) | String | ID da OS |
| `osNumero` | String | Número legível |
| `status` | String | NA_FILA / EM_EXECUCAO / FINALIZADA / CANCELADA |
| `clienteNome` | String | Nome do cliente |
| `veiculoPlaca` | String | Placa do veículo |
| `itensServico` | StringSet | Lista de serviços |
| `enfileiradoEm` | String | Timestamp |
| `iniciadoEm` | String | Timestamp |
| `finalizadoEm` | String | Timestamp |
