-- Correções de nome feitas pelo usuário: valem só pra ele, sem mexer no catálogo global.
CREATE TABLE IF NOT EXISTS produto_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id),
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    nome VARCHAR(200) NOT NULL,
    marca VARCHAR(100),
    categoria VARCHAR(50),
    data_inclusao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_produto_usuario UNIQUE (usuario_id, produto_id)
);
