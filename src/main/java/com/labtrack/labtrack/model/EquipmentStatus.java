package com.labtrack.labtrack.model;

/**
 * Status do equipamento. Os nomes em português são o contrato com o front-end e o valor
 * persistido em equipment.current_status (EnumType.STRING).
 *
 * DANIFICADO bloqueia empréstimo do mesmo jeito que qualquer status diferente de DISPONIVEL:
 * a checagem ainda não existe porque o fluxo de retirada (checkout) não foi implementado.
 */
public enum EquipmentStatus {
    DISPONIVEL,
    EMPRESTADO,
    MANUTENCAO,
    DANIFICADO,
    INATIVO
}
