package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ActiveLoanDTO;
import com.labtrack.labtrack.exception.StudentNotFoundException;
import com.labtrack.labtrack.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Tag(name = "Student", description = "Endpoints para gerenciamento de alunos")
public class StudentController {

    private final StudentService studentService;

    @Operation(
            summary = "Buscar empréstimos ativos do aluno (RF06)",
            description = "Retorna todos os empréstimos ativos de um aluno pela matrícula. " +
                    "Inclui data de retirada, previsão de devolução e lista de equipamentos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de empréstimos ativos retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente")
    })


    @GetMapping("/{matricula}/active-loan")
    public ResponseEntity<List<ActiveLoanDTO>> getEmprestimosAtivos(
            @PathVariable
            @NotBlank(message = "Matrícula não pode ser vazia")
            @Parameter(description = "Número de matrícula do aluno", example = "2021001")
            String matricula) {

        log.info("Requisição GET /api/student/{}/active-loan", matricula);

        List<ActiveLoanDTO> activeLoans = studentService.findActiveLoansByRegistration(matricula);
        log.info("Retornando {} empréstimos ativos para matrícula: {}", activeLoans.size(), matricula);

        return ResponseEntity.ok(activeLoans);
    }


}