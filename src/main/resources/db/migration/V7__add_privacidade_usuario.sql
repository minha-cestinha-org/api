-- Consentimento pro "preço da galera": começa desligado (opt-in) e guarda quando o usuário mudou
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS compartilhar_precos BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS compartilhar_precos_em TIMESTAMP;
