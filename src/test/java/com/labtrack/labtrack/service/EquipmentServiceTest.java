package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    void shouldCreateEquipment_WhenValidRequest() {
        // Arrange
        EquipmentRequestDTO request = EquipmentRequestDTO.builder()
                .name("Multímetro Digital")
                .identificationPhoto("foto.jpg")
                .currentStatus("available")
                .build();

        Equipment savedEquipment = new Equipment();
        savedEquipment.setId(1L);
        savedEquipment.setName(request.getName());
        savedEquipment.setIdentificationPhoto(request.getIdentificationPhoto());
        savedEquipment.setCurrentStatus(request.getCurrentStatus());

        when(equipmentRepository.save(any(Equipment.class))).thenReturn(savedEquipment);

        // Act
        EquipmentResponseDTO response = equipmentService.createEquipment(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Multímetro Digital");
        assertThat(response.getIdentificationPhoto()).isEqualTo("foto.jpg");
    }
}