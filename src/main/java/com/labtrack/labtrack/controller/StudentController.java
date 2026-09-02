package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ActiveLoanDTO;
import com.labtrack.labtrack.dto.StudentCreateRequest;
import com.labtrack.labtrack.dto.StudentResponse;
import com.labtrack.labtrack.exception.StudentNotFoundException;
import com.labtrack.labtrack.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Aluno", description = "Endpoints para gerenciamento de alunos")
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


    @GetMapping("/{registrationNumber}/active-loans")
    public ResponseEntity<List<ActiveLoanDTO>> getActiveLoans(
            @PathVariable
            @NotBlank(message = "Matrícula não pode ser vazia")
            @Parameter(description = "Número de matrícula do aluno", example = "2021001")
            String registrationNumber) {

        log.info("Requisição GET /api/students/{}/active-loans", registrationNumber);

        try {
            List<ActiveLoanDTO> activeLoans = studentService.findActiveLoansByRegistration(registrationNumber);
            log.info("Retornando {} empréstimos ativos para matrícula: {}", activeLoans.size(), registrationNumber);
            return ResponseEntity.ok(activeLoans);

        } catch (StudentNotFoundException e) {
            log.warn("Aluno não encontrado: {}", registrationNumber);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Cadastrar aluno manualmente (RF18)",
            description = "Cadastra um aluno pelo nome, matrícula, e-mail e telefone, sem vinculação a um empréstimo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aluno cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados obrigatórios ausentes ou inválidos"),
            @ApiResponse(responseCode = "409", description = "Matrícula já cadastrada"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente")
    })
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        log.info("Requisição POST /api/students, matrícula: {}", request.registrationNumber());

        StudentResponse response = studentService.createStudent(request);

        log.info("Aluno cadastrado com sucesso, id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/teste")
    public ResponseEntity<String> teste(){
        log.info("Endpoint de teste chamado!");
        return ResponseEntity.ok("Controller funcioando");

    }
}