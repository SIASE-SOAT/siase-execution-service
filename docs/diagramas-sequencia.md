# Diagramas de Sequência — siase-execution-service

---

## 1. Enfileiramento de OS (evento ORCAMENTO_APROVADO)

```
SQS execution-queue   SqsExecutionConsumer   EnfileirarOsUC   DynamoDB (fila)
         │                    │                    │                │
         │── mensagem ────────►│                    │                │
         │                    │── executar() ──────►│                │
         │                    │                    │── findByOsId ──►│
         │                    │                    │◄── empty ───────│
         │                    │                    │── save(fila) ───►│
         │                    │                    │  status: NA_FILA│
         │                    │◄── fila ───────────│                │
         │                    │── deleteMessage ───►│                │
```

---

## 2. Idempotência — ORCAMENTO_APROVADO duplicado

```
SQS execution-queue   SqsExecutionConsumer   EnfileirarOsUC   DynamoDB (fila)
         │                    │                    │                │
         │── mensagem ────────►│                    │                │
         │                    │── executar() ──────►│                │
         │                    │                    │── findByOsId ──►│
         │                    │                    │◄── fila existente│
         │                    │                    │── retorna existente
         │                    │◄── fila ───────────│                │
         │                    │── deleteMessage ───►│                │
         │                    │  (sem novo save)    │                │
```

---

## 3. Mecânico Inicia Execução

```
Mecânico   ExecucaoController   IniciarExecucaoUC   DynamoDB (fila)   PostgreSQL (histórico)   SQS os-updates
    │              │                   │                  │                    │                     │
    │─PATCH /iniciar►│                  │                  │                    │                     │
    │              │── executar() ─────►│                  │                    │                     │
    │              │                   │── findByOsId ────►│                    │                     │
    │              │                   │◄── fila(NA_FILA) ─│                    │                     │
    │              │                   │── fila.iniciar()  │                    │                     │
    │              │                   │── save(fila) ─────►│                    │                     │
    │              │                   │  status:EM_EXECUCAO│                   │                     │
    │              │                   │── save(itemExec) ──────────────────────►│                     │
    │              │                   │── publicarExecucaoIniciada ─────────────────────────────────►│
    │              │◄── fila ──────────│                  │                    │                     │
    │◄── 200 OK ───│                   │                  │                    │                     │
```

---

## 4. Mecânico Finaliza Execução

```
Mecânico   ExecucaoController   FinalizarExecucaoUC   DynamoDB (fila)   PostgreSQL (histórico)   SQS os-updates
    │              │                    │                   │                    │                     │
    │─PATCH /finalizar►│                │                   │                    │                     │
    │              │── executar() ──────►│                   │                    │                     │
    │              │                    │── findByOsId ─────►│                    │                     │
    │              │                    │◄── fila(EM_EXEC) ──│                    │                     │
    │              │                    │── fila.finalizar() │                    │                     │
    │              │                    │── save(fila) ──────►│                    │                     │
    │              │                    │  status:FINALIZADA │                    │                     │
    │              │                    │── save(itemExec) ───────────────────────►│                     │
    │              │                    │── publicarExecucaoFinalizada ────────────────────────────────►│
    │              │◄── fila ───────────│                   │                    │                     │
    │◄── 200 OK ───│                    │                   │                    │                     │
```

---

## 5. Compensação — OS_CANCELADA

```
SQS execution-queue   SqsExecutionConsumer   CancelarExecucaoUC   DynamoDB (fila)
         │                    │                    │                    │
         │── OS_CANCELADA ────►│                    │                    │
         │                    │── executar() ──────►│                    │
         │                    │                    │── findByOsId ──────►│
         │                    │                    │◄── fila ────────────│
         │                    │                    │── fila.cancelar()   │
         │                    │                    │── save(fila) ───────►│
         │                    │                    │  status:CANCELADA   │
         │                    │◄── void ───────────│                    │
         │                    │── deleteMessage ───►│                    │
         │                    │  (sem evento,       │                    │
         │                    │   compensação       │                    │
         │                    │   silenciosa)       │                    │
```
