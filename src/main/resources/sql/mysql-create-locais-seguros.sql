-- Rodar manualmente no MySQL quando spring.jpa.hibernate.ddl-auto=none.

CREATE TABLE IF NOT EXISTS locais_seguros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(120) NULL,
    cidade VARCHAR(120) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    endereco_exibicao VARCHAR(500) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    raio_metros INT NOT NULL DEFAULT 100,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_locais_seguros_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    INDEX idx_locais_seguros_usuario (usuario_id)
);

ALTER TABLE usuario
ADD COLUMN locais_seguros_ativo 
int NOT NULL DEFAULT 0;