package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.LoanRequestDTO;
import com.labtrack.labtrack.dto.LoanResponseDTO;
import com.labtrack.labtrack.service.LoanService;
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

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Endpoints for loan management")
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Registrar empréstimo (RF02, RF11, RF12)",
            description = "Cria o empréstimo com um item por unidade de equipamento, cada um com sua foto e " +
                    "condição de retirada. Cadastra o aluno automaticamente pela matrícula quando ele ainda " +
                    "não existe. Equipamentos que ficam sem unidades livres passam a EMPRESTADO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empréstimo registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados obrigatórios ausentes ou inválidos (ex.: foto)"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente"),
            @ApiResponse(responseCode = "404", description = "Equipamento ou professor não encontrado"),
            @ApiResponse(responseCode = "409", description = "Equipamento indisponível (RN01)"),
            @ApiResponse(responseCode = "422", description = "Aluno bloqueado por atraso (RN05) ou confiabilidade (RN07)")
    })
    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(
            @Valid @RequestBody LoanRequestDTO request,
            Principal principal) {

        log.info("Requisição POST /api/loans - professorId={}, itens={}",
                request.getProfessorId(), request.getItems() == null ? 0 : request.getItems().size());

        LoanResponseDTO response = loanService.createLoan(request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
