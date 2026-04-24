-- Seed manual para ambiente local (MySQL) baseado no mock-api/db.json
-- Uso sugerido:
--   mysql -u root -p softwave < seed_from_dbjson.sql

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM comprovante;
DELETE FROM notificacao;
DELETE FROM transacao;
DELETE FROM honorario;
DELETE FROM usuarios_processos;
DELETE FROM documento_pessoal;
DELETE FROM processo;
DELETE FROM usuario;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO usuario (
    id, tipo_usuario, email, senha, nome, telefone, cpf, foto,
    logradouro, numero, complemento, bairro, cidade, cep, cnpj, nome_fantasia, razao_social,
    created_at, updated_at
) VALUES
    (
        1, 'advogado_juridico', 'advogado@softwave.com', '123456', 'Silva & Associados', '(11)3456-7890', NULL, NULL,
        'Av. Paulista', '1000', NULL, 'Bela Vista', 'Sao Paulo', '01310-100',
        '12.345.678/0001-90', 'Silva & Associados', 'Silva e Associados Sociedade de Advogados',
        '2025-01-01 09:00:00', '2026-03-01 09:00:00'
    ),
    (
        2, 'usuario_fisico', 'cliente@softwave.com', '123456', 'Joao Silva', '(11)98765-4321', '123.456.789-00', NULL,
        'Av. Paulista', '1500', NULL, 'Bela Vista', 'Sao Paulo', '01310-200',
        NULL, NULL, NULL,
        '2025-02-01 09:00:00', '2026-03-01 09:00:00'
    );

INSERT INTO processo (
    id, numero_processo, descricao, area, classe, vara, foro, data_fim, categoria, created_at, updated_at
) VALUES
    (1, 'Processo 1234/2025', 'Acao civel de cobranca', 'Civel', 'Procedimento comum', '1a Vara Civel', 'Sao Paulo', NULL, 'honorarios', '2025-02-01 09:00:00', '2026-03-01 09:00:00'),
    (2, 'Processo 5678/2025', 'Consultoria contratual', 'Empresarial', 'Consultivo', '2a Vara Civel', 'Sao Paulo', NULL, 'consultoria', '2025-05-10 14:00:00', '2026-03-01 09:00:00'),
    (3, 'Processo 9012/2025', 'Acompanhamento mensal', 'Trabalhista', 'Execucao', '3a Vara do Trabalho', 'Sao Paulo', NULL, 'fixo_mensal', '2025-08-01 10:30:00', '2026-03-01 09:00:00'),
    (4, 'Processo 3456/2024', 'Contrato encerrado', 'Civel', 'Procedimento comum', '4a Vara Civel', 'Sao Paulo', '2025-01-01', 'honorarios', '2024-06-15 11:00:00', '2025-01-02 09:00:00');

INSERT INTO usuarios_processos (usuario_id, processo_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4),
    (2, 1), (2, 2);

INSERT INTO honorario (
    id, processo_id, titulo, valor_total, data_inicio, data_fim, status, parcelas
) VALUES
    (1, 1, 'Honorarios - Processo 1234', 2500000.00, '2025-02-01', '2026-05-15', 'encerrado', 5),
    (2, 2, 'Honorarios - Processo 5678', 1800000.00, '2025-05-10', '2026-06-20', 'pendente', 6),
    (3, 3, 'Honorarios - Processo 9012', 1200000.00, '2025-08-01', '2026-04-10', 'encerrado', 4),
    (4, 4, 'Honorarios - Processo 3456', 800000.00, '2024-06-15', '2025-01-01', 'encerrado', 4);

INSERT INTO transacao (
    id, honorario_id, titulo, valor, tipo, status_financeiro, status_aprovacao,
    data_emissao, data_vencimento, data_pagamento, descricao, observacoes, contraparte
) VALUES
    (1, 1, 'Honorarios - Processo 1234', 500000.00, 'receita', 'pago', 'aprovado', '2026-03-10', '2026-03-15', '2026-03-12', 'Parcela 3/5', NULL, 'Joao Silva'),
    (2, 2, 'Custas Judiciais', 85000.00, 'despesa', 'pendente', 'pendente', '2026-03-08', '2026-03-20', NULL, 'Custas do processo 5678', NULL, 'Tribunal'),
    (3, 2, 'Honorarios - Consultoria', 320000.00, 'receita', 'atrasado', 'pendente', '2026-02-01', '2026-02-10', NULL, 'Consultoria contratual', NULL, 'Maria Santos'),
    (4, 2, 'Aluguel do Escritorio', 450000.00, 'despesa', 'pago', 'aprovado', '2026-03-01', '2026-03-05', '2026-03-05', 'Despesa fixa mensal', NULL, 'Imobiliaria Paulista'),
    (5, 3, 'Honorarios - Processo 9012', 800000.00, 'receita', 'pago', 'aprovado', '2026-03-12', '2026-03-25', '2026-03-18', 'Parcela 3/4', NULL, 'Carlos Oliveira'),
    (6, 1, 'Nova transacao', 100000.00, 'receita', 'pago', 'aprovado', '2026-06-01', '2026-06-01', '2026-06-01', 'Lancamento manual', NULL, 'Receita avulsa');

INSERT INTO comprovante (
    id, transacao_id, nome_arquivo, caminho_arquivo, data_upload
) VALUES
    (1, 1, 'comprovante_tx1.pdf', '/uploads/comprovante_tx1.pdf', '2026-03-12 14:00:00');

INSERT INTO notificacao (
    id, usuario_id, titulo, mensagem, tipo, lida, data_criacao
) VALUES
    (1, 1, 'Novo pagamento recebido', 'Joao Silva realizou o pagamento de R$ 5.000,00', 'pagamento', TRUE, '2026-03-12 16:00:00'),
    (2, 1, 'Pagamento pendente', 'Honorarios de Maria Santos vence amanha', 'alerta', TRUE, '2026-03-11 16:00:00'),
    (3, 1, 'Pagamento confirmado', 'Comprovante aprovado para o processo 1234', 'sucesso', TRUE, '2026-03-10 16:00:00'),
    (4, 2, 'Pagamento confirmado', 'Seu pagamento de R$ 6.000,00 foi confirmado', 'pagamento', FALSE, '2026-03-12 14:30:00'),
    (5, 2, 'Nova cobranca disponivel', 'Parcela de R$ 7.000,00 vence em 15/04/2026', 'cobranca', FALSE, '2026-03-12 09:15:00');

INSERT INTO documento_pessoal (
    id, nome_arquivo, url_arquivo, s3_key, fk_usuario_id
) VALUES
    (1, 'doc_cliente_rg.pdf', 'https://example.com/docs/doc_cliente_rg.pdf', 'docs/cliente/rg.pdf', 2);
