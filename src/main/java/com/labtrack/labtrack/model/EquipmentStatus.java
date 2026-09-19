package com.labtrack.labtrack.model;

/**
 * Status do equipamento. Os nomes em português são o contrato com o front-end e o valor
 * persistido em equipment.current_status (EnumType.STRING).
 */
public enum EquipmentStatus {
    DISPONIVEL,
    EMPRESTADO,
    MANUTENCAO,
    DANIFICADO,
    INATIVO
}
