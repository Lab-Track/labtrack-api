package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.labtrack.labtrack.model.EquipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO para alteração manual do status de um equipamento")
public class EquipmentStatusUpdateRequestDTO {

    @JsonProperty("status")
    @NotNull(message = "Status é obrigatório")
    @Schema(description = "Novo status do equipamento", example = "MANUTENCAO")
    private EquipmentStatus status;

    @JsonProperty("motivo")
    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    @Schema(description = "Motivo da alteração (opcional)", example = "Display com defeito")
    private String reason;
}
