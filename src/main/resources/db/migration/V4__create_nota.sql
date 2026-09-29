CREATE TABLE IF NOT EXISTS nota (
    id BIGSERIAL PRIMARY KEY,
    chave_acesso VARCHAR(44) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id),
    mercado_id BIGINT NOT NULL REFERENCES mercado (id),
    data_emissao TIMESTAMP NOT NULL,
    valor_total NUMERIC(12, 2) NOT NULL,
    descontos NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_pago NUMERIC(12, 2) NOT NULL,
    origem VARCHAR(10) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_inclusao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_nota_usuario_chave UNIQUE (usuario_id, chave_acesso)
);

CREATE TABLE IF NOT EXISTS item_nota (
    id BIGSERIAL PRIMARY KEY,
    nota_id BIGINT NOT NULL REFERENCES nota (id),
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    numero INTEGER NOT NULL,
    descricao_bruta VARCHAR(200) NOT NULL,
    codigo VARCHAR(30),
    quantidade NUMERIC(12, 4) NOT NULL,
    unidade VARCHAR(10) NOT NULL,
    preco_unitario NUMERIC(12, 4) NOT NULL,
    preco_total NUMERIC(12, 2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_nota_usuario_emissao ON nota (usuario_id, data_emissao);
CREATE INDEX IF NOT EXISTS idx_item_nota_nota ON item_nota (nota_id);
CREATE INDEX IF NOT EXISTS idx_item_nota_produto ON item_nota (produto_id);
