package com.labtrack.labtrack.exception;

public class LoanReturnNotAllowedException extends RuntimeException {

    public LoanReturnNotAllowedException(String message) {
        super(message);
    }
}
