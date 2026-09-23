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
@Schema(description = "DTO de resposta do upload de foto")
public class PhotoUploadResponseDTO {

    @Schema(description = "URL publica da foto salva", example = "http://localhost:8080/uploads/equipment-photos/3f2a1c1e.jpg")
    private String fotoUrl;
}
