package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.dto.EquipmentStatusUpdateRequestDTO;
import com.labtrack.labtrack.dto.ProjectResponseDTO;
import com.labtrack.labtrack.exception.EquipmentDeletionNotAllowedException;
import com.labtrack.labtrack.exception.EquipmentNotFoundException;
import com.labtrack.labtrack.exception.EquipmentStatusChangeNotAllowedException;
import com.labtrack.labtrack.exception.ProjectNotFoundException;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.LoanItem;
import com.labtrack.labtrack.model.LoanReturn;
import com.labtrack.labtrack.model.Project;
import com.labtrack.labtrack.model.StatusHistory;
import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanReturnRepository;
import com.labtrack.labtrack.repository.ProjectRepository;
import com.labtrack.labtrack.repository.StatusHistoryRepository;
import com.labtrack.labtrack.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private static final String EVENT_TYPE_CHECKOUT = "RETIRADA";
    private static final String EVENT_TYPE_RETURN = "DEVOLUCAO";
    private static final String ITEM_STATUS_LOANED = "loaned";
    private static final String DEFAULT_CATEGORY = "Sem categoria";

    private final EquipmentRepository equipmentRepository;
    private final LoanItemRepository loanItemRepository;
    private final LoanReturnRepository loanReturnRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final TechnicianRepository technicianRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public EquipmentResponseDTO createEquipment(EquipmentRequestDTO request) {
        log.info("Criando novo equipamento: {}", request.getName());

        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        // placeholder unico ate o INSERT gerar o id; o codigo real depende dele (EQP-0001 = id 1)
        equipment.setCode(UUID.randomUUID().toString());
        equipment.setIdentificationPhoto(request.getIdentificationPhoto());
        equipment.setCurrentStatus(
                request.getCurrentStatus() != null ? request.getCurrentStatus() : EquipmentStatus.DISPONIVEL);
        equipment.setCategory(
                request.getCategory() != null && !request.getCategory().isBlank()
                        ? request.getCategory() : DEFAULT_CATEGORY);
        equipment.setLaboratory(request.getLaboratory());
        equipment.setQuantity(request.getQuantity());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));
            equipment.setProject(project);
        }

        Equipment savedEquipment = equipmentRepository.save(equipment);

        savedEquipment.setCode(generateCode(savedEquipment.getId()));
        savedEquipment = equipmentRepository.save(savedEquipment);

        log.info("Equipamento criado com ID: {} e codigo: {}", savedEquipment.getId(), savedEquipment.getCode());

        return mapToResponseDTO(savedEquipment);
    }

    private String generateCode(Long id) {
        return String.format("EQP-%04d", id);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentResponseDTO> findAll(
            EquipmentStatus status, String search, Long projectId, Pageable pageable) {
        log.info("Listando equipamentos - status={}, search={}, projectId={}", status, search, projectId);

        if (projectId != null && !projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        return equipmentRepository.search(status, search, projectId, pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public EquipmentResponseDTO findById(Long equipmentId) {
        log.info("Buscando equipamento id: {}", equipmentId);
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
        return mapToResponseDTO(equipment);
    }

    @Transactional
    public void deleteEquipment(Long equipmentId) {
        log.info("Excluindo equipamento id: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));

        if (hasActiveLoan(equipment)) {
            throw new EquipmentDeletionNotAllowedException(
                    "Equipamento com empréstimo ativo não pode ser excluído");
        }

        if (loanItemRepository.existsByEquipmentId(equipmentId)) {
            throw new EquipmentDeletionNotAllowedException(
                    "Equipamento com histórico de empréstimos não pode ser excluído; use o status INATIVO");
        }

        statusHistoryRepository.deleteByEquipmentId(equipmentId);
        equipmentRepository.delete(equipment);

        log.info("Equipamento excluído com ID: {}", equipmentId);
    }

    @Transactional
    public EquipmentResponseDTO updateStatus(
            Long equipmentId, EquipmentStatusUpdateRequestDTO request, String technicianLogin) {
        log.info("Alterando status do equipamento id: {} para {}", equipmentId, request.getStatus());

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));

        EquipmentStatus previousStatus = equipment.getCurrentStatus();
        EquipmentStatus newStatus = request.getStatus();

        // PATCH idempotente: repetir o status atual não gera registro no histórico.
        if (newStatus == previousStatus) {
            return mapToResponseDTO(equipment);
        }

        if (newStatus == EquipmentStatus.EMPRESTADO) {
            throw new EquipmentStatusChangeNotAllowedException(
                    "O status EMPRESTADO é definido pelo empréstimo e não pode ser alterado manualmente");
        }

        if (hasActiveLoan(equipment)) {
            throw new EquipmentStatusChangeNotAllowedException(
                    "Equipamento com empréstimo ativo não pode ter o status alterado");
        }

        Technician technician = technicianRepository.findByLogin(technicianLogin)
                .orElseThrow(() -> new IllegalStateException(
                        "Técnico autenticado não encontrado: " + technicianLogin));

        StatusHistory history = new StatusHistory();
        history.setEquipment(equipment);
        history.setPreviousStatus(previousStatus.name());
        history.setNewStatus(newStatus.name());
        history.setChangeDate(LocalDateTime.now());
        history.setTechnician(technician);
        history.setReason(request.getReason());
        statusHistoryRepository.save(history);

        equipment.setCurrentStatus(newStatus);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        log.info("Status do equipamento id: {} alterado de {} para {}", equipmentId, previousStatus, newStatus);

        return mapToResponseDTO(savedEquipment);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentHistoryDTO> findLoanHistoryByEquipmentId(Long equipmentId, Pageable pageable) {
        log.info("Buscando histórico de empréstimos do equipamento id: {}", equipmentId);

        equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));

        List<EquipmentHistoryDTO> history = new ArrayList<>();

        loanItemRepository.findByEquipmentId(equipmentId).stream()
                .collect(Collectors.groupingBy(item -> item.getLoan().getId()))
                .values()
                .forEach(items -> history.add(toCheckoutDTO(items)));

        loanReturnRepository.findByEquipmentId(equipmentId).stream()
                .collect(Collectors.groupingBy(
                        lr -> Map.entry(lr.getLoanItem().getLoan().getId(), lr.getReturnDate())))
                .values()
                .forEach(returns -> history.add(toReturnDTO(returns)));

        history.sort(Comparator.comparing(EquipmentHistoryDTO::getEventDate).reversed());

        log.info("Encontrados {} eventos de histórico para equipamento id: {}", history.size(), equipmentId);

        return paginate(history, pageable);
    }

    // O status vale para a linha inteira; os loan_item são a fonte de verdade de empréstimo em aberto.
    private boolean hasActiveLoan(Equipment equipment) {
        return equipment.getCurrentStatus() == EquipmentStatus.EMPRESTADO
                || loanItemRepository.existsByEquipmentIdAndItemStatus(equipment.getId(), ITEM_STATUS_LOANED);
    }

    private EquipmentResponseDTO mapToResponseDTO(Equipment equipment) {
        long loanedItems = loanItemRepository
                .countByEquipmentIdAndItemStatus(equipment.getId(), ITEM_STATUS_LOANED);

        return EquipmentResponseDTO.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .code(equipment.getCode())
                .identificationPhoto(equipment.getIdentificationPhoto())
                .currentStatus(equipment.getCurrentStatus())
                .category(equipment.getCategory())
                .laboratory(equipment.getLaboratory())
                .project(mapProjectToDTO(equipment.getProject()))
                .availableQuantity(equipment.getQuantity() - (int) loanedItems)
                .quantity(equipment.getQuantity())
                .createdAt(equipment.getCreatedAt())
                .build();
    }

    private ProjectResponseDTO mapProjectToDTO(Project project) {
        if (project == null) {
            return null;
        }
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .professorName(project.getProfessor().getName())
                .build();
    }

    // Uma retirada agrupa todos os LoanItem do mesmo empréstimo: checkout_date é do Loan,
    // então todas as unidades retiradas juntas compartilham a mesma data.
    private EquipmentHistoryDTO toCheckoutDTO(List<LoanItem> items) {
        Loan loan = items.get(0).getLoan();
        return EquipmentHistoryDTO.builder()
                .loanId(loan.getId())
                .eventType(EVENT_TYPE_CHECKOUT)
                .eventDate(loan.getCheckoutDate())
                .studentName(loan.getStudent().getName())
                .professorName(loan.getResponsibleProfessor().getName())
                .quantity(items.size())
                .build();
    }

    // Uma devolução agrupa os LoanReturn do mesmo empréstimo com a mesma return_date: unidades
    // devolvidas em momentos diferentes (devolução parcial) continuam como eventos separados.
    private EquipmentHistoryDTO toReturnDTO(List<LoanReturn> returns) {
        LoanReturn first = returns.get(0);
        Loan loan = first.getLoanItem().getLoan();
        return EquipmentHistoryDTO.builder()
                .loanId(loan.getId())
                .eventType(EVENT_TYPE_RETURN)
                .eventDate(first.getReturnDate())
                .studentName(loan.getStudent().getName())
                .professorName(loan.getResponsibleProfessor().getName())
                .quantity(returns.size())
                .build();
    }

    private Page<EquipmentHistoryDTO> paginate(List<EquipmentHistoryDTO> history, Pageable pageable) {
        long total = history.size();
        long start = pageable.getOffset();

        if (start >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        int startIndex = (int) start;
        int endIndex = (int) Math.min(start + pageable.getPageSize(), total);
        return new PageImpl<>(history.subList(startIndex, endIndex), pageable, total);
    }
}
