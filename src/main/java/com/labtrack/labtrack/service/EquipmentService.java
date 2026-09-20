package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.exception.DuplicateEquipmentCodeException;
import com.labtrack.labtrack.exception.EquipmentDeletionNotAllowedException;
import com.labtrack.labtrack.exception.EquipmentNotFoundException;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.LoanItem;
import com.labtrack.labtrack.model.LoanReturn;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanReturnRepository;
import com.labtrack.labtrack.repository.StatusHistoryRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private static final String EVENT_TYPE_CHECKOUT = "RETIRADA";
    private static final String EVENT_TYPE_RETURN = "DEVOLUCAO";
    private static final String ITEM_STATUS_LOANED = "loaned";

    private final EquipmentRepository equipmentRepository;
    private final LoanItemRepository loanItemRepository;
    private final LoanReturnRepository loanReturnRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    @Transactional
    public EquipmentResponseDTO createEquipment(EquipmentRequestDTO request) {
        log.info("Criando novo equipamento: {}", request.getName());

        if (equipmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateEquipmentCodeException(request.getCode());
        }

        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setCode(request.getCode());
        equipment.setIdentificationPhoto(request.getIdentificationPhoto());
        equipment.setCurrentStatus(
                request.getCurrentStatus() != null ? request.getCurrentStatus() : EquipmentStatus.DISPONIVEL);
        equipment.setCategory(request.getCategory());
        equipment.setLaboratory(request.getLaboratory());
        equipment.setQuantity(request.getQuantity());

        Equipment savedEquipment = equipmentRepository.save(equipment);
        log.info("Equipamento criado com ID: {}", savedEquipment.getId());

        return mapToResponseDTO(savedEquipment);
    }

    @Transactional
    public void deleteEquipment(Long equipmentId) {
        log.info("Excluindo equipamento id: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));

        // O status vale para a linha inteira; os loan_item são a fonte de verdade de empréstimo em aberto.
        if (equipment.getCurrentStatus() == EquipmentStatus.EMPRESTADO
                || loanItemRepository.existsByEquipmentIdAndItemStatus(equipmentId, ITEM_STATUS_LOANED)) {
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

    @Transactional(readOnly = true)
    public Page<EquipmentHistoryDTO> findLoanHistoryByEquipmentId(Long equipmentId, Pageable pageable) {
        log.info("Buscando histórico de empréstimos do equipamento id: {}", equipmentId);

        equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));

        List<EquipmentHistoryDTO> history = new ArrayList<>();

        loanItemRepository.findByEquipmentId(equipmentId)
                .forEach(item -> history.add(toCheckoutDTO(item)));

        loanReturnRepository.findByEquipmentId(equipmentId)
                .forEach(loanReturn -> history.add(toReturnDTO(loanReturn)));

        history.sort(Comparator.comparing(EquipmentHistoryDTO::getEventDate).reversed());

        log.info("Encontrados {} eventos de histórico para equipamento id: {}", history.size(), equipmentId);

        return paginate(history, pageable);
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
                .availableQuantity(equipment.getQuantity() - (int) loanedItems)
                .quantity(equipment.getQuantity())
                .createdAt(equipment.getCreatedAt())
                .build();
    }

    private EquipmentHistoryDTO toCheckoutDTO(LoanItem loanItem) {
        Loan loan = loanItem.getLoan();
        return toHistoryDTO(loan, EVENT_TYPE_CHECKOUT, loan.getCheckoutDate());
    }

    private EquipmentHistoryDTO toReturnDTO(LoanReturn loanReturn) {
        Loan loan = loanReturn.getLoanItem().getLoan();
        return toHistoryDTO(loan, EVENT_TYPE_RETURN, loanReturn.getReturnDate());
    }

    private EquipmentHistoryDTO toHistoryDTO(Loan loan, String eventType, LocalDateTime eventDate) {
        return EquipmentHistoryDTO.builder()
                .loanId(loan.getId())
                .eventType(eventType)
                .eventDate(eventDate)
                .studentName(loan.getStudent().getName())
                .professorName(loan.getResponsibleProfessor().getName())
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
