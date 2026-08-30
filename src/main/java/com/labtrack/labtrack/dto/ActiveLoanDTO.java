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
@Schema(description = "DTO para empréstimos ativos de um aluno")
public class ActiveLoanDTO {

    @Schema(description = "ID do empréstimo", example = "1")
    private Long loanId;

    @Schema(description = "Data de retirada", example = "2026-08-30T10:30:00")
    private LocalDateTime checkoutDate;

    @Schema(description = "Previsão de devolução", example = "2026-09-06T10:30:00")
    private LocalDateTime expectedReturnDate;

    @Schema(description = "Data de prorrogação (se houver)", example = "2026-09-10T10:30:00")
    private LocalDateTime extendedDate;

    @Schema(description = "Status do empréstimo", example = "ACTIVE")
    private String loanStatus;

    @Schema(description = "Lista de itens do empréstimo")
    private List<ActiveLoanItemDTO> items;
}