package com.labtrack.labtrack.exception;

public class EquipmentNotFoundException extends RuntimeException {

    public EquipmentNotFoundException(String message) {
        super(message);
    }

    public EquipmentNotFoundException(Long equipmentId) {
        super("Equipamento não encontrado com id: " + equipmentId);
    }

    public EquipmentNotFoundException(Long equipmentId, Throwable cause) {
        super("Equipamento não encontrado com id: " + equipmentId, cause);
    }
}
