package com.labtrack.labtrack.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(Long projectId) {
        super("Projeto não encontrado com id: " + projectId);
    }
}
