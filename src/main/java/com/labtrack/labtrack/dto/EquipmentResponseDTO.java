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
@Schema(description = "DTO para resposta de cadastro de equipamento")
public class EquipmentResponseDTO {

    @Schema(description = "ID do equipamento", example = "1")
    private Long id;

    @Schema(description = "Nome do equipamento", example = "Multímetro Digital")
    private String name;

    @Schema(description = "Foto de identificação", example = "equipamentos/multimetro.jpg")
    private String identificationPhoto;

    @Schema(description = "Status atual do equipamento", example = "available")
    private String currentStatus;

    @Schema(description = "Localização física do equipamento", example = "Armário SparkImp")
    private String location;

    @Schema(description = "Quantidade de unidades em estoque", example = "1")
    private Integer quantity;
}