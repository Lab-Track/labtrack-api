package com.labtrack.labtrack.exception;

public class StudentNotFoundException extends RuntimeException {

  public StudentNotFoundException(String registration) {
    super("Aluno não encontrado com matrícula: " + registration);
  }

  public StudentNotFoundException(String registration, Throwable cause) {
    super("Aluno não encontrado com matrícula: " + registration, cause);
  }
}