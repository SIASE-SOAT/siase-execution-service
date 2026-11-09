# RFC-001 — Contratos de Eventos SQS do Execution Service

**Status:** Aprovado  
**Data:** 2025-01

## Resumo

Define os eventos que o Execution Service consome e produz no fluxo de execução de serviços.

## Eventos Consumidos

### ORCAMENTO_APROVADO (fila: `siase-execution-queue`)
```json
{
  "tipo": "ORCAMENTO_APROVADO",
  "osId": "uuid",
  "osNumero": "OS-20250101-ABC123",
  "clienteNome": "João Silva",
  "veiculoPlaca": "ABC-1234",
  "total": "350.00",
  "status": "APROVADO"
}
```
**Ação:** Enfileirar OS para execução no DynamoDB com status `NA_FILA`.

### OS_CANCELADA (fila: `siase-execution-queue`)
```json
{
  "tipo": "OS_CANCELADA",
  "osId": "uuid",
  "osNumero": "OS-20250101-ABC123",
  "motivo": "Cliente desistiu"
}
```
**Ação:** Cancelar execução na fila (compensação Saga). Se já `FINALIZADA`, ignora.

## Eventos Produzidos

### EXECUCAO_INICIADA (fila: `siase-os-updates-queue`)
```json
{
  "tipo": "EXECUCAO_INICIADA",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid"
}
```
**Efeito:** OS Service avança OS para `EM_EXECUCAO`.

### EXECUCAO_FINALIZADA (fila: `siase-os-updates-queue`)
```json
{
  "tipo": "EXECUCAO_FINALIZADA",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid"
}
```
**Efeito:** OS Service avança OS para `FINALIZADA`. Billing Service inicia cobrança via Mercado Pago.

## Idempotência

O `osId` é chave primária no DynamoDB. Enfileirar a mesma OS duas vezes retorna a entrada existente sem criar duplicata.

## Fluxo de Execução Manual

O mecânico interage via REST:
```
GET  /api/execucao/fila              → visualiza fila
PATCH /api/execucao/fila/{osId}/iniciar   → inicia execução
PATCH /api/execucao/fila/{osId}/finalizar → finaliza execução
```
Cada ação persiste no DynamoDB (estado) e no PostgreSQL (histórico) e publica o evento correspondente.
