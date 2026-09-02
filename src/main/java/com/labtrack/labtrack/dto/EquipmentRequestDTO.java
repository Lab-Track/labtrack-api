package com.labtrack.labtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para requisição de cadastro de equipamento")
public class EquipmentRequestDTO {

    @NotBlank(message = "Nome do equipamento é obrigatório")
    @Schema(description = "Nome do equipamento", example = "Multímetro Digital")
    private String name;

    @NotBlank(message = "Foto de identificação é obrigatória")
    @Schema(description = "URL ou caminho da foto de identificação", example = "equipamentos/multimetro.jpg")
    private String identificationPhoto;

    @Schema(description = "Descrição do estado atual", example = "Em bom estado")
    private String currentStatus;
}