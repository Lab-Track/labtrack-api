package com.labtrack.labtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item de um empréstimo ativo")
public class ActiveLoanItemDTO {

    @Schema(description = "ID do item do empréstimo", example = "1")
    private Long loanItemId;

    @Schema(description = "ID do equipamento", example = "1")
    private Long equipmentId;

    @Schema(description = "Nome do equipamento", example = "Multímetro Digital")
    private String equipmentName;

    @Schema(description = "Foto do equipamento na retirada", example = "multimetro_retirada.jpg")
    private String checkoutPhoto;

    @Schema(description = "Condição do equipamento na retirada", example = "GOOD")
    private String checkoutCondition;

    @Schema(description = "Status do item", example = "ACTIVE")
    private String itemStatus;
}