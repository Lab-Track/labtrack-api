package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de projeto, usado na listagem de projetos e no campo projeto do equipamento")
public class ProjectResponseDTO {

    @Schema(description = "ID do projeto", example = "5")
    private Long id;

    @JsonProperty("nome")
    @Schema(description = "Nome do projeto", example = "Sensores IoT")
    private String name;

    @JsonProperty("professorResponsavel")
    @Schema(description = "Nome do professor responsável pelo projeto", example = "Carlos Lima")
    private String professorName;
}
