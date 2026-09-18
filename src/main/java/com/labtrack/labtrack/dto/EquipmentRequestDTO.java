package com.labtrack.labtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(description = "Localização física do equipamento", example = "Armário SparkImp")
    private String location;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior ou igual a 1")
    @Schema(description = "Quantidade de unidades em estoque", example = "1")
    private Integer quantity;
}