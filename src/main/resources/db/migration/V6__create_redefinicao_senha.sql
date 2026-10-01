CREATE TABLE IF NOT EXISTS redefinicao_senha (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id),
    codigo_hash VARCHAR(255) NOT NULL,
    expira_em TIMESTAMP NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    usado_em TIMESTAMP,
    data_inclusao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_redefinicao_senha_usuario ON redefinicao_senha (usuario_id, data_inclusao);

-- Vai no token; trocar a senha incrementa e derruba os tokens antigos
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS versao_token INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS senha_alterada_em TIMESTAMP;
