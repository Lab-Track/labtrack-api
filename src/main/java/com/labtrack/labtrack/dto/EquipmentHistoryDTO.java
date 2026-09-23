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
@Schema(description = "DTO para um evento do histórico de empréstimos de um equipamento")
public class EquipmentHistoryDTO {

    @Schema(description = "ID do empréstimo relacionado ao evento", example = "1")
    private Long loanId;

    @Schema(description = "Tipo do evento: RETIRADA ou DEVOLUCAO", example = "RETIRADA")
    private String eventType;

    @Schema(description = "Data em que o evento ocorreu", example = "2026-08-30T10:30:00")
    private LocalDateTime eventDate;

    @Schema(description = "Nome do aluno responsável pelo empréstimo", example = "João Silva")
    private String studentName;

    @Schema(description = "Nome do professor responsável pelo empréstimo", example = "Prof. Maria Souza")
    private String professorName;

    @Schema(description = "Quantidade de unidades cobertas por este evento (mesmo empréstimo, " +
            "mesmo tipo de evento e mesma data)", example = "2")
    private Integer quantity;
}