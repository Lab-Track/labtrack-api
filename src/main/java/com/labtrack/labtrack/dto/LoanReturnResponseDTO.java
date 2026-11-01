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
@Schema(description = "DTO de resposta do registro de devolução")
public class LoanReturnResponseDTO {

    @Schema(description = "ID do empréstimo", example = "1")
    private Long loanId;

    @Schema(description = "Status do empréstimo após a devolução", example = "partial")
    private String loanStatus;

    @Schema(description = "Quantidade de unidades ainda emprestadas (não devolvidas)", example = "1")
    private int remainingQuantity;

    @Schema(description = "IDs dos itens de empréstimo devolvidos nesta chamada")
    private List<Long> returnedLoanItemIds;

    @Schema(description = "Data da devolução", example = "2026-10-21T10:30:00")
    private LocalDateTime returnDate;

    @Schema(description = "Indica se a devolução ocorreu após o prazo previsto")
    private boolean overdue;
}
