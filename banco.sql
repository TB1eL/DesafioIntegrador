-- ============================================================
--  NeoEletro - NiceSystem  |  Script DDL + DML
--  Banco: gestao_pedidos
-- ============================================================

CREATE DATABASE IF NOT EXISTS gestao_pedidos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gestao_pedidos;

-- ------------------------------------------------------------
-- Clientes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id    INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE
);

-- ------------------------------------------------------------
-- Produtos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produtos (
    id        INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    preco     DECIMAL(10,2) NOT NULL CHECK (preco > 0),
    estoque   INT           NOT NULL DEFAULT 0 CHECK (estoque >= 0),
    categoria ENUM('NOTEBOOK','SMARTPHONE','PERIFERICO','COMPONENTE','ACESSORIO') NOT NULL
);

-- ------------------------------------------------------------
-- Pedidos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedidos (
    id         INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    status     ENUM('ABERTO','FILA','PROCESSANDO','FINALIZADO') NOT NULL DEFAULT 'ABERTO',
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- ------------------------------------------------------------
-- Itens do Pedido
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS itens_pedido (
    id          INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pedido_id   INT           NOT NULL,
    produto_id  INT           NOT NULL,
    quantidade  INT           NOT NULL CHECK (quantidade > 0),
    preco_unit  DECIMAL(10,2) NOT NULL CHECK (preco_unit > 0),
    CONSTRAINT fk_item_pedido   FOREIGN KEY (pedido_id)  REFERENCES pedidos(id),
    CONSTRAINT fk_item_produto  FOREIGN KEY (produto_id) REFERENCES produtos(id)
);

-- ============================================================
-- DML - Dados iniciais
-- ============================================================

INSERT INTO clientes (nome, email) VALUES
    ('Gabriel Caldas',   'biel.caldas@email.com'),
    ('Matheus Machado',  'matheusmachado@email.com'),
    ('João Fortkamp',    'joaofortkamp@email.com');

INSERT INTO produtos (nome, preco, estoque, categoria) VALUES
    ('Notebook Dell Inspiron 15',    3599.90, 10, 'NOTEBOOK'),
    ('Notebook Lenovo IdeaPad 3',    2899.90,  8, 'NOTEBOOK'),
    ('iPhone 15 Pro',                7999.90, 15, 'SMARTPHONE'),
    ('Samsung Galaxy S24',           4799.90, 20, 'SMARTPHONE'),
    ('Mouse Logitech MX Master 3',    399.90, 35, 'PERIFERICO'),
    ('Teclado Mecânico Redragon',     299.90, 40, 'PERIFERICO'),
    ('Monitor LG 27" IPS',           1499.90, 12, 'PERIFERICO'),
    ('SSD Kingston 1TB NVMe',         399.90, 25, 'COMPONENTE'),
    ('Memória RAM Corsair 16GB DDR5', 349.90, 18, 'COMPONENTE'),
    ('Placa de Vídeo RTX 4060',      2799.90,  5, 'COMPONENTE'),
    ('Cabo USB-C 2m',                  39.90, 80, 'ACESSORIO'),
    ('Suporte para Notebook',          129.90, 30, 'ACESSORIO'),
    ('Headset HyperX Cloud II',        499.90, 22, 'PERIFERICO');
