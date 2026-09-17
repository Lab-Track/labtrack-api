package com.labtrack.labtrack.exception;

public class DuplicateRegistrationNumberException extends RuntimeException {

    public DuplicateRegistrationNumberException(String registrationNumber) {
        super("Matrícula já cadastrada: " + registrationNumber);
    }
}
