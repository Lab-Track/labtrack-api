package com.labtrack.labtrack.exception;

public class LoanNotAllowedException extends RuntimeException {

    public LoanNotAllowedException(String message) {
        super(message);
    }
}
