-- A V9 removeu o CHECK de verification_status pra aceitar texto livre, mas a coluna
-- continuava VARCHAR(20) — pequena demais para os rótulos em português do front
-- ("Funcionando normalmente", "Funcionamento parcial", "Não funciona").
ALTER TABLE loan_return ALTER COLUMN verification_status TYPE VARCHAR(255);
