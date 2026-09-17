package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    @Transactional
    public EquipmentResponseDTO createEquipment(EquipmentRequestDTO request) {
        log.info("Criando novo equipamento: {}", request.getName());

        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setIdentificationPhoto(request.getIdentificationPhoto());
        equipment.setCurrentStatus(request.getCurrentStatus() != null ? request.getCurrentStatus() : "available");
        equipment.setLocation(request.getLocation());
        equipment.setQuantity(request.getQuantity());

        Equipment savedEquipment = equipmentRepository.save(equipment);
        log.info("Equipamento criado com ID: {}", savedEquipment.getId());

        return mapToResponseDTO(savedEquipment);
    }

    private EquipmentResponseDTO mapToResponseDTO(Equipment equipment) {
        return EquipmentResponseDTO.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .identificationPhoto(equipment.getIdentificationPhoto())
                .currentStatus(equipment.getCurrentStatus())
                .location(equipment.getLocation())
                .quantity(equipment.getQuantity())
                .build();
    }
}