package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ProjectResponseDTO;
import com.labtrack.labtrack.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Endpoints for project lookup")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Listar projetos",
            description = "Lista todos os projetos cadastrados, usados para vincular equipamentos " +
                    "e para o filtro de projeto no catálogo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de projetos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente")
    })
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getProjects() {
        log.info("Requisição GET /api/projects");
        List<ProjectResponseDTO> projects = projectService.findAll();
        log.info("Retornando {} projetos", projects.size());
        return ResponseEntity.ok(projects);
    }
}
