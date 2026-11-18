# RFC-001 — Contratos de Eventos SQS do Execution Service

**Status:** Aprovado
**Data:** 2025-01

## Resumo

Define os eventos que o Execution Service consome e produz. O envelope segue o padrão do Saga: `tipoEvento`, `osId`, `numeroOs`, `payload` (JSON string opcional), `ocorridoEm`.

## Eventos Consumidos (fila: `siase-execution-queue`)

### ORCAMENTO_APROVADO
```json
{
  "tipoEvento": "ORCAMENTO_APROVADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"clienteNome\":\"João Silva\",\"veiculo\":\"Fiat Uno\",\"valorOrcamento\":350.00,\"servicos\":[{\"descricao\":\"Troca de óleo\",\"preco\":80.00}]}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Ação:** Enfileirar OS no DynamoDB com status `NA_FILA`. O consumer extrai `clienteNome`, `veiculo` e `servicos` do payload para popular a fila. Idempotente: `osId` é PK no DynamoDB.

### OS_CANCELADA
```json
{
  "tipoEvento": "OS_CANCELADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": null,
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Ação:** Cancelar item na fila (compensação Saga). Se OS não estava na fila, registra no histórico e ignora. Publica `EXECUCAO_CANCELADA`.

## Eventos Produzidos (fila: `siase-os-updates-queue`)

### EXECUCAO_INICIADA
```json
{
  "tipoEvento": "EXECUCAO_INICIADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"mecanico\":\"Carlos\"}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `EM_EXECUCAO`. O campo `mecanico` no payload é opcional.

### EXECUCAO_FINALIZADA
```json
{
  "tipoEvento": "EXECUCAO_FINALIZADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "Execução finalizada",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `FINALIZADA`.

### EXECUCAO_CANCELADA
```json
{
  "tipoEvento": "EXECUCAO_CANCELADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "Execução cancelada por compensação",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service registra no histórico; sem mudança de status (OS já está `CANCELADA`).

## Idempotência

O `osId` é chave primária no DynamoDB. Enfileirar a mesma OS duas vezes retorna a entrada existente sem criar duplicata.

## Fluxo de Execução Manual

O mecânico interage via REST:
```
GET  /api/execucao/fila              → visualiza fila (itens NA_FILA)
GET  /api/execucao                   → todos os itens + histórico
POST /api/execucao/{osId}/iniciar    → inicia execução (param opcional: mecanico)
POST /api/execucao/{osId}/finalizar  → finaliza execução
```
Cada ação persiste no DynamoDB (estado) e no PostgreSQL (histórico) e publica o evento correspondente.
