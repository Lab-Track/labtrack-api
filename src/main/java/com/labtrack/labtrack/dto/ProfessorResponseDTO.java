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
@Schema(description = "DTO de professor, usado na seleção de professor responsável pelo empréstimo")
public class ProfessorResponseDTO {

    @Schema(description = "ID do professor", example = "1")
    private Long id;

    @Schema(description = "Nome do professor", example = "Carlos Lima")
    private String name;
}
