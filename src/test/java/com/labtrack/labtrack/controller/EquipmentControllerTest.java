package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.dto.EquipmentStatusUpdateRequestDTO;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentControllerTest {

    @Mock
    private EquipmentService equipmentService;

    @InjectMocks
    private EquipmentController equipmentController;

    @Test
    void shouldReturn200WithPagedHistory_WhenEquipmentHasEvents() {
        // Arrange
        EquipmentHistoryDTO event = EquipmentHistoryDTO.builder()
                .loanId(11L)
                .eventType("RETIRADA")
                .eventDate(LocalDateTime.of(2026, 9, 5, 14, 0))
                .studentName("Bruno Lima")
                .professorName("Carlos Lima")
                .build();

        Page<EquipmentHistoryDTO> page = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1);

        when(equipmentService.findLoanHistoryByEquipmentId(1L, PageRequest.of(0, 10)))
                .thenReturn(page);

        // Act
        ResponseEntity<Page<EquipmentHistoryDTO>> response = equipmentController
                .getLoanHistory(1L, 0, 10);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getEventType()).isEqualTo("RETIRADA");
    }

    @Test
    void shouldReturn200WithPagedEquipment_WhenListingWithoutFilters() {
        // Arrange
        EquipmentResponseDTO item = EquipmentResponseDTO.builder()
                .id(1L)
                .currentStatus(EquipmentStatus.DISPONIVEL)
                .build();
        Page<EquipmentResponseDTO> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

        when(equipmentService.findAll(null, null, null, PageRequest.of(0, 10))).thenReturn(page);

        // Act
        ResponseEntity<Page<EquipmentResponseDTO>> response =
                equipmentController.getEquipment(null, null, null, 0, 10);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void shouldReturn200WithFilteredEquipment_WhenStatusAndSearchProvided() {
        // Arrange
        Page<EquipmentResponseDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(equipmentService.findAll(EquipmentStatus.MANUTENCAO, "osc", null, PageRequest.of(0, 10)))
                .thenReturn(page);

        // Act
        ResponseEntity<Page<EquipmentResponseDTO>> response =
                equipmentController.getEquipment(EquipmentStatus.MANUTENCAO, "osc", null, 0, 10);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    void shouldReturn200WithFilteredEquipment_WhenProjectIdProvided() {
        // Arrange
        EquipmentResponseDTO item = EquipmentResponseDTO.builder()
                .id(1L)
                .currentStatus(EquipmentStatus.DISPONIVEL)
                .build();
        Page<EquipmentResponseDTO> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(equipmentService.findAll(null, null, 5L, PageRequest.of(0, 10))).thenReturn(page);

        // Act
        ResponseEntity<Page<EquipmentResponseDTO>> response =
                equipmentController.getEquipment(null, null, 5L, 0, 10);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void shouldReturn200WithEquipment_WhenFoundById() {
        // Arrange
        EquipmentResponseDTO dto = EquipmentResponseDTO.builder().id(1L).build();
        when(equipmentService.findById(1L)).thenReturn(dto);

        // Act
        ResponseEntity<EquipmentResponseDTO> response = equipmentController.getEquipmentById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void shouldReturn200WithUpdatedEquipment_WhenStatusIsUpdated() {
        // Arrange
        EquipmentStatusUpdateRequestDTO request = EquipmentStatusUpdateRequestDTO.builder()
                .status(EquipmentStatus.MANUTENCAO)
                .reason("Display com defeito")
                .build();
        EquipmentResponseDTO updated = EquipmentResponseDTO.builder()
                .id(1L)
                .currentStatus(EquipmentStatus.MANUTENCAO)
                .build();
        Principal principal = () -> "tecnico.teste";

        when(equipmentService.updateStatus(1L, request, "tecnico.teste")).thenReturn(updated);

        // Act
        ResponseEntity<EquipmentResponseDTO> response =
                equipmentController.updateEquipmentStatus(1L, request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCurrentStatus()).isEqualTo(EquipmentStatus.MANUTENCAO);
    }

    @Test
    void shouldReturn204_WhenEquipmentIsDeleted() {
        // Act
        ResponseEntity<Void> response = equipmentController.deleteEquipment(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(equipmentService).deleteEquipment(1L);
    }

    @Test
    void shouldReturn200WithEmptyContent_WhenEquipmentHasNoEvents() {
        // Arrange
        Page<EquipmentHistoryDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(equipmentService.findLoanHistoryByEquipmentId(1L, PageRequest.of(0, 10)))
                .thenReturn(page);

        // Act
        ResponseEntity<Page<EquipmentHistoryDTO>> response = equipmentController
                .getLoanHistory(1L, 0, 10);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
    }
}