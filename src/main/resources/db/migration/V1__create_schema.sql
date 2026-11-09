CREATE TABLE IF NOT EXISTS itens_execucao (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    os_id           VARCHAR(36)   NOT NULL,
    os_numero       VARCHAR(30)   NOT NULL,
    servico_nome    VARCHAR(255),
    mecanico        VARCHAR(255),
    iniciado_em     TIMESTAMP,
    finalizado_em   TIMESTAMP,
    criado_em       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_itens_exec_os_id ON itens_execucao(os_id);
