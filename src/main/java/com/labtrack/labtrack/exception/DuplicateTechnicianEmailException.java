package com.labtrack.labtrack.exception;

public class DuplicateTechnicianEmailException extends RuntimeException {

    public DuplicateTechnicianEmailException(String email) {
        super("E-mail já cadastrado: " + email);
    }
}
