package com.labtrack.labtrack.exception;

public class EquipmentDeletionNotAllowedException extends RuntimeException {

    public EquipmentDeletionNotAllowedException(String message) {
        super(message);
    }
}
