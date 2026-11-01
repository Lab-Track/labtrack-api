package com.labtrack.labtrack.exception;

public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException(Long loanId) {
        super("Empréstimo não encontrado com id: " + loanId);
    }
}
