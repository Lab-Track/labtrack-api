-- Motivo informado pelo técnico ao alterar manualmente o status de um equipamento (opcional).
ALTER TABLE status_history ADD COLUMN reason VARCHAR(500);
