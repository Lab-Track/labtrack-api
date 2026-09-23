package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.labtrack.labtrack.model.EquipmentStatus;
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
@Schema(description = "DTO de equipamento, com os mesmos nomes de campo do tipo Equipamento do front-end")
public class EquipmentResponseDTO {

    @Schema(description = "ID do equipamento", example = "1")
    private Long id;

    @JsonProperty("nome")
    @Schema(description = "Nome do equipamento", example = "Multímetro Digital")
    private String name;

    @JsonProperty("codigo")
    @Schema(description = "Código único do equipamento", example = "EQP-0001")
    private String code;

    @JsonProperty("fotoUrl")
    @Schema(description = "Foto de identificação", example = "equipamentos/multimetro.jpg")
    private String identificationPhoto;

    @JsonProperty("status")
    @Schema(description = "Status atual do equipamento", example = "DISPONIVEL")
    private EquipmentStatus currentStatus;

    @JsonProperty("categoria")
    @Schema(description = "Categoria do equipamento", example = "Medição")
    private String category;

    @JsonProperty("laboratorio")
    @Schema(description = "Laboratório onde o equipamento fica", example = "Laboratório de Eletrônica")
    private String laboratory;

    @JsonProperty("projeto")
    @Schema(description = "Projeto ao qual o equipamento está vinculado, se houver")
    private ProjectResponseDTO project;

    @JsonProperty("qtdDisponivel")
    @Schema(description = "Unidades disponíveis: total menos os itens de empréstimo ainda emprestados", example = "2")
    private Integer availableQuantity;

    @JsonProperty("qtdTotal")
    @Schema(description = "Quantidade total de unidades", example = "3")
    private Integer quantity;

    @JsonProperty("cadastradoEm")
    @Schema(description = "Data e hora do cadastro", example = "2026-09-19T10:30:00")
    private LocalDateTime createdAt;
}
