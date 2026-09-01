-- Test technician fixture for local login testing.
-- Plaintext password: Senha@123 (BCrypt hash below), documented in
-- docs/superpowers/specs/2026-08-31-jwt-auth-login-design.md and README.md.
INSERT INTO technician (name, login, password_hash, email)
VALUES (
    'Tecnico de Teste',
    'tecnico.teste',
    '$2a$10$DPzLXRwFEnumlebrgTIYteAy9If9ONgrdWqi5PKp/h9V7niqqbbr2',
    'tecnico.teste@labtrack.local'
);
