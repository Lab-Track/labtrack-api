CREATE TABLE professor (
    id_professor BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE aluno (
    id_aluno BIGSERIAL PRIMARY KEY,
    matricula VARCHAR(50) UNIQUE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    taxa_confiabilidade NUMERIC(5,2) NOT NULL DEFAULT 100,
    data_cadastro TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tecnico (
    id_tecnico BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE projeto (
    id_projeto BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    id_professor BIGINT NOT NULL REFERENCES professor(id_professor)
);

CREATE TABLE equipamento (
    id_equipamento BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    foto_identificacao VARCHAR(500) NOT NULL,
    estado_atual VARCHAR(20) NOT NULL
        CHECK (estado_atual IN ('disponivel', 'emprestado', 'manutencao', 'danificado')),
    id_projeto BIGINT REFERENCES projeto(id_projeto)
);

CREATE TABLE historico_status (
    id_historico BIGSERIAL PRIMARY KEY,
    id_equipamento BIGINT NOT NULL REFERENCES equipamento(id_equipamento),
    status_anterior VARCHAR(20) NOT NULL,
    status_novo VARCHAR(20) NOT NULL,
    data_alteracao TIMESTAMP NOT NULL DEFAULT now(),
    id_tecnico BIGINT NOT NULL REFERENCES tecnico(id_tecnico)
);

CREATE TABLE emprestimo (
    id_emprestimo BIGSERIAL PRIMARY KEY,
    id_aluno BIGINT NOT NULL REFERENCES aluno(id_aluno),
    id_professor_responsavel BIGINT NOT NULL REFERENCES professor(id_professor),
    id_tecnico BIGINT NOT NULL REFERENCES tecnico(id_tecnico),
    data_hora_retirada TIMESTAMP NOT NULL,
    data_prevista_devolucao TIMESTAMP NOT NULL,
    data_prorrogada TIMESTAMP,
    status_emprestimo VARCHAR(20) NOT NULL
        CHECK (status_emprestimo IN ('andamento', 'atrasado', 'devolvido', 'parcial'))
);

CREATE TABLE item_emprestimo (
    id_item_emprestimo BIGSERIAL PRIMARY KEY,
    id_emprestimo BIGINT NOT NULL REFERENCES emprestimo(id_emprestimo),
    id_equipamento BIGINT NOT NULL REFERENCES equipamento(id_equipamento),
    foto_retirada VARCHAR(500) NOT NULL,
    estado_retirada VARCHAR(255) NOT NULL,
    status_item VARCHAR(20) NOT NULL
        CHECK (status_item IN ('emprestado', 'devolvido', 'removido'))
);

CREATE TABLE devolucao (
    id_devolucao BIGSERIAL PRIMARY KEY,
    id_item_emprestimo BIGINT NOT NULL UNIQUE REFERENCES item_emprestimo(id_item_emprestimo),
    data_hora_devolucao TIMESTAMP NOT NULL,
    estado_devolucao VARCHAR(255) NOT NULL,
    observacoes VARCHAR(1000),
    status_verificacao VARCHAR(20) NOT NULL
        CHECK (status_verificacao IN ('funcionando', 'defeito', 'nao_testado')),
    atraso BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE notificacao (
    id_notificacao BIGSERIAL PRIMARY KEY,
    id_emprestimo BIGINT NOT NULL REFERENCES emprestimo(id_emprestimo),
    data_envio TIMESTAMP NOT NULL,
    canal VARCHAR(50) NOT NULL,
    status_envio VARCHAR(20) NOT NULL
);
