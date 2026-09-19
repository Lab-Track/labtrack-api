package com.labtrack.labtrack.exception;

public class DuplicateEquipmentCodeException extends RuntimeException {

    public DuplicateEquipmentCodeException(String code) {
        super("Código de equipamento já cadastrado: " + code);
    }
}
