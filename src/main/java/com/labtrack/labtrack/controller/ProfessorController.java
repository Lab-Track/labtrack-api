package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ProfessorResponseDTO;
import com.labtrack.labtrack.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/professors")
@RequiredArgsConstructor
@Tag(name = "Professors", description = "Endpoints for professor lookup")
public class ProfessorController {

    private final ProfessorService professorService;

    @Operation(
            summary = "Listar professores",
            description = "Lista todos os professores cadastrados, usados para selecionar o professor " +
                    "responsável no registro de empréstimo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de professores retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente")
    })
    @GetMapping
    public ResponseEntity<List<ProfessorResponseDTO>> getProfessors() {
        log.info("Requisição GET /api/professors");
        List<ProfessorResponseDTO> professors = professorService.findAll();
        log.info("Retornando {} professores", professors.size());
        return ResponseEntity.ok(professors);
    }
}
