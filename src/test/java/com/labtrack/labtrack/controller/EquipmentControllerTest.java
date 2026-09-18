package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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