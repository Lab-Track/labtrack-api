package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.labtrack.labtrack.model.EquipmentStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Garante que o JSON de equipamento usa os mesmos nomes de campo do tipo Equipamento do front-end
 * (labtrack-webapp, src/features/equipment/types.ts), sem mapeamento manual do lado do front.
 */
class EquipmentDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldSerializeResponseWithFrontendFieldNames() {
        // Arrange
        EquipmentResponseDTO response = EquipmentResponseDTO.builder()
                .id(1L)
                .name("Multímetro Digital")
                .code("EQP-0001")
                .identificationPhoto("foto.jpg")
                .currentStatus(EquipmentStatus.EMPRESTADO)
                .category("Medição")
                .laboratory("Laboratório de Eletrônica")
                .availableQuantity(2)
                .quantity(3)
                .createdAt(LocalDateTime.of(2026, 9, 19, 10, 30))
                .build();

        // Act
        JsonNode json = objectMapper.valueToTree(response);

        // Assert
        List<String> fieldNames = new ArrayList<>();
        json.fieldNames().forEachRemaining(fieldNames::add);
        assertThat(fieldNames).containsExactlyInAnyOrder(
                "id", "nome", "codigo", "fotoUrl", "status", "categoria",
                "laboratorio", "qtdDisponivel", "qtdTotal", "cadastradoEm");
        assertThat(json.get("status").asText()).isEqualTo("EMPRESTADO");
        assertThat(json.get("qtdDisponivel").asInt()).isEqualTo(2);
        assertThat(json.get("qtdTotal").asInt()).isEqualTo(3);
    }

    @Test
    void shouldDeserializeRequestWithFrontendFieldNames() throws JsonProcessingException {
        // Arrange
        String body = """
                {"nome":"Multímetro Digital","codigo":"EQP-0001","fotoUrl":"foto.jpg",
                 "status":"MANUTENCAO","categoria":"Medição","laboratorio":"Laboratório de Eletrônica",
                 "qtdTotal":3}
                """;

        // Act
        EquipmentRequestDTO request = objectMapper.readValue(body, EquipmentRequestDTO.class);

        // Assert
        assertThat(request.getName()).isEqualTo("Multímetro Digital");
        assertThat(request.getCode()).isEqualTo("EQP-0001");
        assertThat(request.getIdentificationPhoto()).isEqualTo("foto.jpg");
        assertThat(request.getCurrentStatus()).isEqualTo(EquipmentStatus.MANUTENCAO);
        assertThat(request.getCategory()).isEqualTo("Medição");
        assertThat(request.getLaboratory()).isEqualTo("Laboratório de Eletrônica");
        assertThat(request.getQuantity()).isEqualTo(3);
    }

    @Test
    void shouldRejectRequest_WhenStatusIsNotInEnum() {
        // Arrange
        String body = """
                {"nome":"X","codigo":"C","fotoUrl":"f","status":"available",
                 "categoria":"c","laboratorio":"l","qtdTotal":1}
                """;

        // Act & Assert
        assertThatThrownBy(() -> objectMapper.readValue(body, EquipmentRequestDTO.class))
                .isInstanceOf(JsonProcessingException.class);
    }
}
