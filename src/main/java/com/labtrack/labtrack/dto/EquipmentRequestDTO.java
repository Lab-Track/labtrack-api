package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.labtrack.labtrack.model.EquipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
@Schema(description = "DTO para requisição de cadastro de equipamento")
public class EquipmentRequestDTO {

    @JsonProperty("nome")
    @NotBlank(message = "Nome do equipamento é obrigatório")
    @Schema(description = "Nome do equipamento", example = "Multímetro Digital")
    private String name;

    @JsonProperty("codigo")
    @NotBlank(message = "Código do equipamento é obrigatório")
    @Size(max = 50, message = "Código deve ter no máximo 50 caracteres")
    @Schema(description = "Código único do equipamento", example = "EQP-0001")
    private String code;

    @JsonProperty("fotoUrl")
    @NotBlank(message = "Foto de identificação é obrigatória")
    @Schema(description = "URL ou caminho da foto de identificação", example = "equipamentos/multimetro.jpg")
    private String identificationPhoto;

    @JsonProperty("status")
    @Schema(description = "Status inicial do equipamento (padrão: DISPONIVEL)", example = "DISPONIVEL")
    private EquipmentStatus currentStatus;

    @JsonProperty("categoria")
    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
    @Schema(description = "Categoria do equipamento", example = "Medição")
    private String category;

    @JsonProperty("laboratorio")
    @NotBlank(message = "Laboratório é obrigatório")
    @Size(max = 255, message = "Laboratório deve ter no máximo 255 caracteres")
    @Schema(description = "Laboratório onde o equipamento fica", example = "Laboratório de Eletrônica")
    private String laboratory;

    @JsonProperty("qtdTotal")
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior ou igual a 1")
    @Schema(description = "Quantidade total de unidades", example = "3")
    private Integer quantity;
}
