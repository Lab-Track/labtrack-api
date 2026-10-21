package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "DTO para registro de devolução (RF04)")
public class LoanReturnRequestDTO {

    @JsonProperty("quantidade")
    @NotNull(message = "Quantidade devolvida é obrigatória")
    @Min(value = 1, message = "Quantidade devolvida deve ser maior ou igual a 1")
    @Schema(description = "Quantidade de unidades devolvidas nesta devolução", example = "1")
    private Integer quantity;

    @JsonProperty("estadoDevolucao")
    @NotBlank(message = "Estado do equipamento na devolução é obrigatório")
    @Size(max = 255, message = "Estado de devolução deve ter no máximo 255 caracteres")
    @Schema(description = "Condição do equipamento na devolução", example = "Sem avarias")
    private String returnCondition;

    @JsonProperty("fotoDevolucao")
    @NotBlank(message = "Foto de devolução é obrigatória")
    @Size(max = 500, message = "Foto de devolução deve ter no máximo 500 caracteres")
    @Schema(description = "URL ou caminho da foto do equipamento na devolução",
            example = "/uploads/equipment-photos/abc.jpg")
    private String returnPhoto;

    @JsonProperty("verificacaoFuncionamento")
    @NotBlank(message = "Verificação de funcionamento é obrigatória")
    @Size(max = 255, message = "Verificação de funcionamento deve ter no máximo 255 caracteres")
    @Schema(description = "Resultado da verificação de funcionamento", example = "Funcionando normalmente")
    private String verificationStatus;

    @JsonProperty("observacoes")
    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    @Schema(description = "Observações sobre o equipamento devolvido (opcional)")
    private String notes;
}
