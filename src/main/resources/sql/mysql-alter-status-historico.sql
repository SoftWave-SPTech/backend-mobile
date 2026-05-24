CREATE TABLE IF NOT EXISTS status_historico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transacao_id INT NOT NULL,
    status_anterior VARCHAR(50) NULL,
    status_novo VARCHAR(50) NOT NULL,
    usuario_id INT NULL,
    motivo TEXT NULL,
    data_criacao DATETIME NOT NULL,
    CONSTRAINT fk_status_historico_transacao
        FOREIGN KEY (transacao_id) REFERENCES transacao(id)
);

