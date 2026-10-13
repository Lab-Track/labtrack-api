package com.labtrack.labtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta do registro de empréstimo")
public class LoanResponseDTO {

    @Schema(description = "ID do empréstimo", example = "1")
    private Long loanId;

    @Schema(description = "ID do aluno", example = "1")
    private Long studentId;

    @Schema(description = "Nome do aluno", example = "Ana Souza")
    private String studentName;

    @Schema(description = "Matrícula do aluno (nula quando o aluno não tem matrícula)", example = "2021001")
    private String studentRegistrationNumber;

    @Schema(description = "ID do professor responsável", example = "1")
    private Long professorId;

    @Schema(description = "Nome do professor responsável", example = "Carlos Lima")
    private String professorName;

    @Schema(description = "Data de retirada", example = "2026-10-13T10:30:00")
    private LocalDateTime checkoutDate;

    @Schema(description = "Previsão de devolução", example = "2026-10-20T10:30:00")
    private LocalDateTime expectedReturnDate;

    @Schema(description = "Status do empréstimo", example = "in_progress")
    private String loanStatus;

    @Schema(description = "Itens do empréstimo")
    private List<ActiveLoanItemDTO> items;
}
