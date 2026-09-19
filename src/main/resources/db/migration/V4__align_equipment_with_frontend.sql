-- Status: valores em inglês minúsculo -> enum em português (contrato com o front-end).
-- INATIVO é um conceito novo e distinto de 'damaged' (DANIFICADO); nenhuma linha existente vira INATIVO.
ALTER TABLE equipment DROP CONSTRAINT equipment_current_status_check;

UPDATE equipment
SET current_status = CASE current_status
    WHEN 'available'   THEN 'DISPONIVEL'
    WHEN 'loaned'      THEN 'EMPRESTADO'
    WHEN 'maintenance' THEN 'MANUTENCAO'
    WHEN 'damaged'     THEN 'DANIFICADO'
END;

UPDATE status_history
SET previous_status = CASE previous_status
        WHEN 'available'   THEN 'DISPONIVEL'
        WHEN 'loaned'      THEN 'EMPRESTADO'
        WHEN 'maintenance' THEN 'MANUTENCAO'
        WHEN 'damaged'     THEN 'DANIFICADO'
        ELSE previous_status
    END,
    new_status = CASE new_status
        WHEN 'available'   THEN 'DISPONIVEL'
        WHEN 'loaned'      THEN 'EMPRESTADO'
        WHEN 'maintenance' THEN 'MANUTENCAO'
        WHEN 'damaged'     THEN 'DANIFICADO'
        ELSE new_status
    END;

ALTER TABLE equipment
    ADD CONSTRAINT equipment_current_status_check
        CHECK (current_status IN ('DISPONIVEL', 'EMPRESTADO', 'MANUTENCAO', 'DANIFICADO', 'INATIVO'));

-- Campos novos. Linhas existentes recebem valores derivados/padrão antes de virarem NOT NULL.
ALTER TABLE equipment
    ADD COLUMN code VARCHAR(50),
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();

UPDATE equipment
SET code = 'EQP-' || LPAD(id::text, 4, '0'),
    category = 'Sem categoria';

-- location (campo único) passa a ser laboratory (não há bancada).
ALTER TABLE equipment RENAME COLUMN location TO laboratory;

UPDATE equipment SET laboratory = 'Não informado' WHERE laboratory IS NULL;

ALTER TABLE equipment
    ALTER COLUMN code SET NOT NULL,
    ALTER COLUMN category SET NOT NULL,
    ALTER COLUMN laboratory SET NOT NULL,
    ADD CONSTRAINT uk_equipment_code UNIQUE (code);
