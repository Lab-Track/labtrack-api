-- verification_status passa a ser texto livre, no mesmo padrão de checkout_condition/
-- return_condition (que já não têm CHECK): evita mapear os rótulos em português do front
-- ("Funcionando normalmente", "Funcionamento parcial", "Não funciona") para um enum fixo em
-- inglês que não tinha espaço pra "parcial".
ALTER TABLE loan_return DROP CONSTRAINT loan_return_verification_status_check;

-- Foto do estado do equipamento na devolução, no mesmo padrão de loan_item.checkout_photo.
-- Tabela ainda vazia em todo ambiente (endpoint de devolução nunca existiu), então dá pra
-- adicionar NOT NULL direto, sem precisar de default/backfill.
ALTER TABLE loan_return ADD COLUMN return_photo VARCHAR(500) NOT NULL;
