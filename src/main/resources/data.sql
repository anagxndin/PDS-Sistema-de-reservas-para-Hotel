-- Dados de teste para desenvolvimento. Roda automaticamente no boot da
-- aplicacao (Spring Boot executa data.sql apos o Hibernate criar/atualizar
-- as tabelas via ddl-auto: update).
-- Se a tabela ja tiver esses registros, o INSERT falharia por causa da
-- unicidade de "nome" (nao ha constraint unica nesse campo, entao roda
-- sempre e pode duplicar em reinicializacoes sem reset do banco).

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Quarto Solteiro Standard', 'Quarto compacto e aconchegante, ideal para uma pessoa.', 'SOLTEIRO', 1, 120.00, 5, 'https://placehold.co/600x400?text=solteiro', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Quarto Solteiro Standard');

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Quarto Solteiro Confort', 'Versao com varanda e vista para o jardim.', 'SOLTEIRO', 1, 145.00, 3, 'https://placehold.co/600x400?text=solteiro', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Quarto Solteiro Confort');

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Quarto Duplo Standard', 'Quarto espacoso para duas pessoas, com cama de casal.', 'DUPLO', 2, 180.00, 8, 'https://placehold.co/600x400?text=duplo', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Quarto Duplo Standard');

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Quarto Duplo Twin', 'Duas camas de solteiro, ideal para amigos viajando juntos.', 'DUPLO', 2, 175.00, 6, 'https://placehold.co/600x400?text=duplo', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Quarto Duplo Twin');

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Suite Premium', 'Suite com area de estar separada e vista para o jardim.', 'SUITE', 3, 320.00, 3, 'https://placehold.co/600x400?text=suite', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Suite Premium');

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Suite Master', 'A maior suite do hotel, com banheira e sacada privativa.', 'SUITE', 2, 480.00, 2, 'https://placehold.co/600x400?text=suite', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Suite Master');

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Quarto Familia Amplo', 'Quarto amplo com camas adicionais para toda a familia.', 'FAMILIA', 5, 450.00, 4, 'https://placehold.co/600x400?text=familia', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Quarto Familia Amplo');

INSERT INTO acomodacoes (nome, descricao, tipo, capacidade, preco_diaria, quantidade_total, url_imagem, ativo, data_criacao)
SELECT 'Quarto Familia Luxo', 'Duas camas de casal e sofa-cama, para grupos grandes.', 'FAMILIA', 6, 520.00, 2, 'https://placehold.co/600x400?text=familia', true, now()
WHERE NOT EXISTS (SELECT 1 FROM acomodacoes WHERE nome = 'Quarto Familia Luxo');