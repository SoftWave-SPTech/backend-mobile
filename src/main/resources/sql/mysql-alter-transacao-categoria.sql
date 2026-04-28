-- Rodar manualmente no MySQL quando spring.jpa.hibernate.ddl-auto=none.
-- Suporta categoria persistida nas transações e relatórios por categoria.

ALTER TABLE transacao
ADD COLUMN IF NOT EXISTS categoria VARCHAR(100) NULL AFTER tipo;
