-- Criação da sequência
CREATE SEQUENCE CONTA_SEQ
    START WITH 1
    INCREMENT BY 1;

-- Criação da tabela CONTA
CREATE TABLE CONTA (
    id BIGINT NOT NULL DEFAULT NEXTVAL('CONTA_SEQ') PRIMARY KEY,
    data_vencimento DATE,
    data_pagamento DATE,
    valor NUMERIC(19, 2),
    descricao VARCHAR(255),
    situacao VARCHAR(255)
);