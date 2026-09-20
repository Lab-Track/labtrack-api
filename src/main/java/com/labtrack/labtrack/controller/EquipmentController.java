package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.dto.EquipmentStatusUpdateRequestDTO;
import com.labtrack.labtrack.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/equipment")
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

        log.info("Request POST /api/equipment - Name: {}", request.getName());

        EquipmentResponseDTO response = equipmentService.createEquipment(request);
        log.info("Equipment created with ID: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Histórico de empréstimos do equipamento (RF05)",
            description = "Retorna, de forma paginada e em ordem decrescente por data, " +
                    "os eventos de retirada e devolução já registrados para o equipamento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Equipamento não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente")
    })
    @GetMapping("/{id}/history")
    public ResponseEntity<Page<EquipmentHistoryDTO>> getLoanHistory(
            @PathVariable
            @Parameter(description = "ID do equipamento", example = "1")
            Long id,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Número da página não pode ser negativo")
            @Parameter(description = "Número da página (0-based)", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Tamanho da página deve ser maior ou igual a 1")
            @Max(value = 100, message = "Tamanho da página não pode ser maior que 100")
            @Parameter(description = "Tamanho da página", example = "10")
            int size) {

        log.info("Requisição GET /api/equipment/{}/history - page={}, size={}", id, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<EquipmentHistoryDTO> history = equipmentService.findLoanHistoryByEquipmentId(id, pageable);

        log.info("Retornando {} itens (página {}/{}) para equipamento id: {}",
                history.getNumberOfElements(), page + 1, history.getTotalPages(), id);

        return ResponseEntity.ok(history);
    }

    @Operation(
            summary = "Alterar status do equipamento (RF17)",
            description = "Altera manualmente o status de um equipamento (ex.: enviar para manutenção) e " +
                    "registra a alteração no histórico de status. O status EMPRESTADO é definido pelo " +
                    "empréstimo e não pode ser informado; equipamentos com empréstimo ativo não podem ter " +
                    "o status alterado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status ausente ou fora do enum"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente"),
            @ApiResponse(responseCode = "404", description = "Equipamento não encontrado"),
            @ApiResponse(responseCode = "409", description = "Empréstimo ativo ou status EMPRESTADO informado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<EquipmentResponseDTO> updateEquipmentStatus(
            @PathVariable
            @Parameter(description = "ID do equipamento", example = "1")
            Long id,
            @Valid @RequestBody EquipmentStatusUpdateRequestDTO request,
            Principal principal) {

        log.info("Requisição PATCH /api/equipment/{}/status - status={}", id, request.getStatus());

        EquipmentResponseDTO response = equipmentService.updateStatus(id, request, principal.getName());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Excluir equipamento",
            description = "Exclui permanentemente um equipamento sem empréstimo ativo e sem histórico de " +
                    "empréstimos. Equipamentos com histórico devem ser marcados como INATIVO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipamento excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente"),
            @ApiResponse(responseCode = "404", description = "Equipamento não encontrado"),
            @ApiResponse(responseCode = "409", description = "Equipamento com empréstimo ativo ou histórico de empréstimos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable
            @Parameter(description = "ID do equipamento", example = "1")
            Long id) {

        log.info("Requisição DELETE /api/equipment/{}", id);

        equipmentService.deleteEquipment(id);

        return ResponseEntity.noContent().build();
    }
}