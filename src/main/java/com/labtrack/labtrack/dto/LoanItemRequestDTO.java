package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item (uma unidade de equipamento) de uma requisição de empréstimo")
public class LoanItemRequestDTO {

    @JsonProperty("equipamentoId")
    @NotNull(message = "ID do equipamento é obrigatório")
    @Schema(description = "ID do equipamento. Repita o ID para retirar mais de uma unidade", example = "1")
    private Long equipmentId;

    @JsonProperty("fotoRetirada")
    @NotBlank(message = "Foto de retirada é obrigatória")
    @Size(max = 500, message = "Foto de retirada deve ter no máximo 500 caracteres")
    @Schema(description = "URL ou caminho da foto do equipamento na retirada", example = "/uploads/equipment-photos/abc.jpg")
    private String checkoutPhoto;

    @JsonProperty("estadoRetirada")
    @NotBlank(message = "Estado do equipamento na retirada é obrigatório")
    @Size(max = 255, message = "Estado de retirada deve ter no máximo 255 caracteres")
    @Schema(description = "Condição do equipamento na retirada", example = "Sem avarias")
    private String checkoutCondition;
}
