CREATE TABLE IF NOT EXISTS produto (
    id BIGSERIAL PRIMARY KEY,
    ean VARCHAR(14) UNIQUE,
    nome VARCHAR(200) NOT NULL,
    marca VARCHAR(100),
    categoria VARCHAR(50),
    data_inclusao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cache global: a mesma descrição no mesmo mercado sempre aponta pro mesmo produto.
CREATE TABLE IF NOT EXISTS mapeamento_descricao (
    id BIGSERIAL PRIMARY KEY,
    descricao_bruta VARCHAR(200) NOT NULL,
    mercado_id BIGINT NOT NULL REFERENCES mercado (id),
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    data_inclusao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_mapeamento_descricao UNIQUE (descricao_bruta, mercado_id)
);
