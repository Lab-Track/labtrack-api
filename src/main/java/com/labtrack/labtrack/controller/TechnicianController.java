package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.TechnicianCreateRequest;
import com.labtrack.labtrack.dto.TechnicianResponse;
import com.labtrack.labtrack.service.TechnicianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/technician")
@RequiredArgsConstructor
@Tag(name = "Technician", description = "Endpoints para gerenciamento de técnicos")
public class TechnicianController {

    private final TechnicianService technicianService;

    @Operation(
            summary = "Cadastrar técnico (RF09)",
            description = "Cadastra um técnico pelo nome, e-mail e senha. O e-mail também é usado como login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Técnico cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados obrigatórios ausentes ou inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente")
    })
    @PostMapping
    public ResponseEntity<TechnicianResponse> createTechnician(@Valid @RequestBody TechnicianCreateRequest request) {
        log.info("Requisição POST /api/technician, e-mail: {}", request.email());

        TechnicianResponse response = technicianService.createTechnician(request);

        log.info("Técnico cadastrado com sucesso, id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
