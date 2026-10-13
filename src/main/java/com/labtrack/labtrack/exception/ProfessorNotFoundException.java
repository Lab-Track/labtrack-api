package com.labtrack.labtrack.exception;

public class ProfessorNotFoundException extends RuntimeException {

    public ProfessorNotFoundException(Long professorId) {
        super("Professor não encontrado com id: " + professorId);
    }
}
