package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Endpoints for equipment management")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(
            summary = "Create a new equipment (RF01, RF08)",
            description = "Registers a new equipment with name, photo and status description."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment created successfully"),
            @ApiResponse(responseCode = "400", description = "Missing required fields (name or photo)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @PostMapping
    public ResponseEntity<EquipmentResponseDTO> createEquipment(
            @Valid @RequestBody EquipmentRequestDTO request) {

        log.info("Request POST /api/equipments - Name: {}", request.getName());

        EquipmentResponseDTO response = equipmentService.createEquipment(request);
        log.info("Equipment created with ID: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}