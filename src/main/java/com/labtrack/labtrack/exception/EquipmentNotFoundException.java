package com.labtrack.labtrack.exception;

public class EquipmentNotFoundException extends RuntimeException {

    public EquipmentNotFoundException(Long equipmentId) {
        super("Equipamento não encontrado com id: " + equipmentId);
    }
}
