CREATE TABLE historico_execucao (
    id            BIGSERIAL PRIMARY KEY,
    os_id         UUID        NOT NULL,
    evento        VARCHAR(64) NOT NULL,
    detalhe       VARCHAR(2000),
    registrado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_historico_execucao_os ON historico_execucao (os_id, registrado_em);
