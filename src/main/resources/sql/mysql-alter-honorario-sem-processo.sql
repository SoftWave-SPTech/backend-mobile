-- Rodar manualmente no MySQL quando spring.jpa.hibernate.ddl-auto=none.
-- Permite honorário sem processo e rastreia o advogado criador.

ALTER TABLE honorario MODIFY COLUMN processo_id INT NULL;
ALTER TABLE honorario ADD COLUMN advogado_usuario_id INT NULL;
