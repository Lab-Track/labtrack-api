package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
import com.labtrack.labtrack.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Endpoints para gerenciamento de equipamentos")
public class EquipmentController {

    private final EquipmentService equipmentService;

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
            @Parameter(description = "Número da página (0-based)", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Tamanho da página", example = "10")
            int size) {

        log.info("Requisição GET /api/equipment/{}/history - page={}, size={}", id, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<EquipmentHistoryDTO> history = equipmentService.findLoanHistoryByEquipmentId(id, pageable);

        log.info("Retornando {} itens (página {}/{}) para equipamento id: {}",
                history.getNumberOfElements(), page + 1, history.getTotalPages(), id);

        return ResponseEntity.ok(history);
    }
}