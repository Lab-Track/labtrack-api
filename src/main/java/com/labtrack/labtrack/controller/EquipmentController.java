package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.dto.EquipmentStatusUpdateRequestDTO;
import com.labtrack.labtrack.dto.PhotoUploadResponseDTO;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.service.EquipmentService;
import com.labtrack.labtrack.service.PhotoStorageService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Endpoints for equipment management")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final PhotoStorageService photoStorageService;

    @Operation(
            summary = "Listar equipamentos (catálogo)",
            description = "Lista os equipamentos de forma paginada, com filtros opcionais por status, " +
                    "por busca (nome ou código, case-insensitive) e por projeto vinculado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente"),
            @ApiResponse(responseCode = "404", description = "Projeto informado no filtro não encontrado")
    })
    @GetMapping
    public ResponseEntity<Page<EquipmentResponseDTO>> getEquipment(
            @RequestParam(required = false)
            @Parameter(description = "Filtro por status", example = "DISPONIVEL")
            EquipmentStatus status,
            @RequestParam(required = false)
            @Parameter(description = "Busca por nome ou código", example = "osciloscópio")
            String search,
            @RequestParam(required = false)
            @Parameter(description = "Filtro por ID do projeto vinculado", example = "1")
            Long projectId,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Número da página não pode ser negativo")
            @Parameter(description = "Número da página (0-based)", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Tamanho da página deve ser maior ou igual a 1")
            @Max(value = 100, message = "Tamanho da página não pode ser maior que 100")
            @Parameter(description = "Tamanho da página", example = "10")
            int size) {

        log.info("Requisição GET /api/equipment - status={}, search={}, projectId={}, page={}, size={}",
                status, search, projectId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<EquipmentResponseDTO> result = equipmentService.findAll(status, search, projectId, pageable);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Detalhar equipamento",
            description = "Retorna os dados de um equipamento pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipamento retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente"),
            @ApiResponse(responseCode = "404", description = "Equipamento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponseDTO> getEquipmentById(
            @PathVariable
            @Parameter(description = "ID do equipamento", example = "1")
            Long id) {

        log.info("Requisição GET /api/equipment/{}", id);
        return ResponseEntity.ok(equipmentService.findById(id));
    }

    @Operation(
            summary = "Create a new equipment (RF01, RF08)",
            description = "Registers a new equipment with name, photo and status description. " +
                    "projetoId is optional; when informed, links the equipment to that project."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment created successfully"),
            @ApiResponse(responseCode = "400", description = "Missing required fields (name or photo)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "projetoId informado não corresponde a nenhum projeto")
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
            summary = "Upload de foto de equipamento",
            description = "Recebe um arquivo JPG ou PNG de até 5MB, salva em disco e retorna a URL publica " +
                    "para uso no campo fotoUrl do cadastro de equipamento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto salva com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo ausente, fora do formato aceito (JPG/PNG) ou maior que 5MB"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Token JWT inválido ou ausente")
    })
    @PostMapping(value = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponseDTO> uploadPhoto(
            @Parameter(description = "Arquivo JPG ou PNG, ate 5MB")
            @RequestPart("foto")
            MultipartFile foto) {

        log.info("Requisição POST /api/equipment/photos - originalFilename={}", foto.getOriginalFilename());

        String fotoUrl = photoStorageService.store(foto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PhotoUploadResponseDTO.builder().fotoUrl(fotoUrl).build());
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
                    "o status alterado. DANIFICADO segue a mesma regra de bloqueio de empréstimo que " +
                    "qualquer status diferente de DISPONIVEL: quando o fluxo de retirada existir, um " +
                    "equipamento DANIFICADO não poderá ser emprestado."
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