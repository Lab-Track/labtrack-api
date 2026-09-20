package com.labtrack.labtrack.exception;

public class EquipmentStatusChangeNotAllowedException extends RuntimeException {

    public EquipmentStatusChangeNotAllowedException(String message) {
        super(message);
    }
}
