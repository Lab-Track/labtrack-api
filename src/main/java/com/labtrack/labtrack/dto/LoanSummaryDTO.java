package com.labtrack.labtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo de um empréstimo, usado na listagem de empréstimos em andamento")
public class LoanSummaryDTO {

    @Schema(description = "ID do empréstimo", example = "1")
    private Long loanId;

    @Schema(description = "Nome do equipamento emprestado", example = "Multímetro Digital")
    private String equipmentName;

    @Schema(description = "Quantidade de unidades ainda emprestadas (não devolvidas)", example = "2")
    private int quantity;

    @Schema(description = "Nome do aluno", example = "Ana Souza")
    private String studentName;

    @Schema(description = "Matrícula do aluno", example = "2021001")
    private String studentRegistrationNumber;

    @Schema(description = "Nome do professor responsável", example = "Carlos Lima")
    private String professorName;

    @Schema(description = "Nome do técnico que registrou o empréstimo", example = "Kênia Oliveira")
    private String technicianName;

    @Schema(description = "Data de retirada", example = "2026-10-13T10:30:00")
    private LocalDateTime checkoutDate;

    @Schema(description = "Previsão de devolução", example = "2026-10-20T10:30:00")
    private LocalDateTime expectedReturnDate;

    @Schema(description = "Status do empréstimo", example = "in_progress")
    private String loanStatus;
}
