package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.ActiveLoanItemDTO;
import com.labtrack.labtrack.dto.LoanItemRequestDTO;
import com.labtrack.labtrack.dto.LoanRequestDTO;
import com.labtrack.labtrack.dto.LoanResponseDTO;
import com.labtrack.labtrack.dto.LoanReturnRequestDTO;
import com.labtrack.labtrack.dto.LoanReturnResponseDTO;
import com.labtrack.labtrack.dto.LoanSummaryDTO;
import com.labtrack.labtrack.exception.EquipmentNotAvailableException;
import com.labtrack.labtrack.exception.EquipmentNotFoundException;
import com.labtrack.labtrack.exception.LoanNotAllowedException;
import com.labtrack.labtrack.exception.LoanNotFoundException;
import com.labtrack.labtrack.exception.LoanReturnNotAllowedException;
import com.labtrack.labtrack.exception.ProfessorNotFoundException;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.LoanItem;
import com.labtrack.labtrack.model.LoanReturn;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.StatusHistory;
import com.labtrack.labtrack.model.Student;
import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.LoanReturnRepository;
import com.labtrack.labtrack.repository.ProfessorRepository;
import com.labtrack.labtrack.repository.StatusHistoryRepository;
import com.labtrack.labtrack.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    static final String LOAN_STATUS_IN_PROGRESS = "in_progress";
    static final String LOAN_STATUS_PARTIAL = "partial";
    static final String LOAN_STATUS_RETURNED = "returned";
    static final String ITEM_STATUS_LOANED = "loaned";
    static final String ITEM_STATUS_RETURNED = "returned";
    static final int DEFAULT_LOAN_DAYS = 7;
    // RN07: aluno com confiabilidade abaixo de 50% não pode pegar equipamento emprestado.
    static final BigDecimal MIN_RELIABILITY_RATE = BigDecimal.valueOf(50);

    private final LoanRepository loanRepository;
    private final LoanItemRepository loanItemRepository;
    private final LoanReturnRepository loanReturnRepository;
    private final EquipmentRepository equipmentRepository;
    private final ProfessorRepository professorRepository;
    private final TechnicianRepository technicianRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final StudentService studentService;

    /**
     * Registra um empréstimo (RF02, RF11, RF12, RF16, RN01, RN04, RN05, RN07) numa única transação:
     * valida os equipamentos (RN01) antes de tocar no aluno, cadastra/recupera o aluno pela matrícula,
     * aplica os bloqueios do aluno (RN05, RN07), grava 1 Loan + 1 LoanItem por unidade e marca como
     * EMPRESTADO o equipamento que ficou sem unidades livres. A RN04 (foto obrigatória) é validada no DTO.
     */
    @Transactional
    public LoanResponseDTO createLoan(LoanRequestDTO request, String technicianLogin) {
        log.info("Registrando empréstimo: professor={}, itens={}", request.getProfessorId(),
                request.getItems().size());

        Professor professor = professorRepository.findById(request.getProfessorId())
                .orElseThrow(() -> new ProfessorNotFoundException(request.getProfessorId()));

        Technician technician = technicianRepository.findByLogin(technicianLogin)
                .orElseThrow(() -> new IllegalStateException(
                        "Técnico autenticado não encontrado: " + technicianLogin));

        Map<Long, Long> requestedUnits = request.getItems().stream()
                .collect(Collectors.groupingBy(LoanItemRequestDTO::getEquipmentId,
                        LinkedHashMap::new, Collectors.counting()));

        Map<Long, Equipment> equipments = equipmentRepository.findAllByIdForUpdate(requestedUnits.keySet())
                .stream()
                .collect(Collectors.toMap(Equipment::getId, Function.identity()));

        Map<Long, Long> loanedUnits = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> requested : requestedUnits.entrySet()) {
            Equipment equipment = equipments.get(requested.getKey());
            if (equipment == null) {
                throw new EquipmentNotFoundException(requested.getKey());
            }
            loanedUnits.put(equipment.getId(), ensureAvailable(equipment, requested.getValue()));
        }

        Student student = studentService.findOrCreateByRegistrationNumber(
                request.getStudentRegistrationNumber(), request.getStudentName(),
                request.getStudentEmail(), request.getStudentPhone());

        LocalDateTime now = LocalDateTime.now();
        ensureStudentCanBorrow(student, now);

        Loan loan = new Loan();
        loan.setStudent(student);
        loan.setResponsibleProfessor(professor);
        loan.setTechnician(technician);
        loan.setCheckoutDate(now);
        loan.setExpectedReturnDate(request.getExpectedReturnDate() != null
                ? request.getExpectedReturnDate()
                : now.plusDays(DEFAULT_LOAN_DAYS));
        loan.setLoanStatus(LOAN_STATUS_IN_PROGRESS);
        Loan savedLoan = loanRepository.save(loan);

        List<LoanItem> items = new ArrayList<>();
        for (LoanItemRequestDTO itemRequest : request.getItems()) {
            LoanItem item = new LoanItem();
            item.setLoan(savedLoan);
            item.setEquipment(equipments.get(itemRequest.getEquipmentId()));
            item.setCheckoutPhoto(itemRequest.getCheckoutPhoto());
            item.setCheckoutCondition(itemRequest.getCheckoutCondition());
            item.setItemStatus(ITEM_STATUS_LOANED);
            items.add(loanItemRepository.save(item));
        }

        requestedUnits.forEach((equipmentId, units) -> markAsLoanedIfNoUnitsLeft(
                equipments.get(equipmentId), loanedUnits.get(equipmentId) + units, technician, savedLoan));

        log.info("Empréstimo {} registrado para o aluno {}", savedLoan.getId(), student.getId());

        return toResponse(savedLoan, student, professor, items);
    }

    /**
     * Lista os empréstimos ainda não devolvidos (RF03), mais recentes primeiro, para a tela de
     * acompanhamento de empréstimos do técnico.
     */
    @Transactional(readOnly = true)
    public List<LoanSummaryDTO> findAllActiveLoans() {
        log.info("Buscando todos os empréstimos em andamento");

        List<LoanSummaryDTO> loans = loanRepository.findAllActiveOrderByCheckoutDateDesc().stream()
                .map(this::toSummary)
                .toList();

        log.info("Retornando {} empréstimo(s) em andamento", loans.size());
        return loans;
    }

    /**
     * Persiste o atraso: empréstimos abertos com prazo vencido viram 'late'. Chamado pelo job agendado;
     * a RN05 não depende dele (ver LoanRepository.hasOverdueLoan).
     */
    @Transactional
    public int markOverdueLoans() {
        int updated = loanRepository.markOverdueLoans(LocalDateTime.now());
        if (updated > 0) {
            log.info("{} empréstimo(s) marcado(s) como atrasado(s)", updated);
        }
        return updated;
    }

    /**
     * Registra a devolução de uma ou mais unidades de um empréstimo (RF04). Devolve as unidades
     * mais antigas ainda emprestadas (loan_item com item_status='loaned'), grava 1 LoanReturn por
     * unidade devolvida, atualiza o status do empréstimo (partial/returned) e libera o equipamento
     * de volta para DISPONIVEL quando nenhuma unidade dele continua emprestada.
     */
    @Transactional
    public LoanReturnResponseDTO registerReturn(
            Long loanId, LoanReturnRequestDTO request, String technicianLogin) {
        log.info("Registrando devolução do empréstimo {}: quantidade={}", loanId, request.getQuantity());

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        if (LOAN_STATUS_RETURNED.equals(loan.getLoanStatus())) {
            throw new LoanReturnNotAllowedException(
                    "Empréstimo " + loanId + " já foi totalmente devolvido");
        }

        List<LoanItem> loanedItems =
                loanItemRepository.findByLoanIdAndItemStatusOrderById(loanId, ITEM_STATUS_LOANED);
        if (request.getQuantity() > loanedItems.size()) {
            throw new LoanReturnNotAllowedException(
                    "Empréstimo " + loanId + " tem apenas " + loanedItems.size()
                            + " unidade(s) emprestada(s), foram informadas " + request.getQuantity()
                            + " para devolução");
        }

        Technician technician = technicianRepository.findByLogin(technicianLogin)
                .orElseThrow(() -> new IllegalStateException(
                        "Técnico autenticado não encontrado: " + technicianLogin));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = loan.getExtendedDate() != null
                ? loan.getExtendedDate() : loan.getExpectedReturnDate();
        boolean overdue = now.isAfter(dueDate);

        List<LoanItem> itemsToReturn = loanedItems.subList(0, request.getQuantity());
        List<Long> returnedLoanItemIds = new ArrayList<>();
        for (LoanItem item : itemsToReturn) {
            item.setItemStatus(ITEM_STATUS_RETURNED);
            loanItemRepository.save(item);

            LoanReturn loanReturn = new LoanReturn();
            loanReturn.setLoanItem(item);
            loanReturn.setReturnDate(now);
            loanReturn.setReturnCondition(request.getReturnCondition());
            loanReturn.setReturnPhoto(request.getReturnPhoto());
            loanReturn.setNotes(request.getNotes());
            loanReturn.setVerificationStatus(request.getVerificationStatus());
            loanReturn.setOverdue(overdue);
            loanReturnRepository.save(loanReturn);

            returnedLoanItemIds.add(item.getId());
        }

        int remaining = loanedItems.size() - itemsToReturn.size();
        loan.setLoanStatus(remaining == 0 ? LOAN_STATUS_RETURNED : LOAN_STATUS_PARTIAL);
        loanRepository.save(loan);

        // Empréstimos com vários tipos de equipamento ainda não existem (RF09), então todo
        // LoanItem de um mesmo Loan compartilha o mesmo equipamento (ver toSummary()).
        releaseEquipmentIfNoUnitsLeft(itemsToReturn.get(0).getEquipment(), technician, loan);

        log.info("Devolução registrada: empréstimo {} agora '{}' ({} unidade(s) ainda emprestada(s))",
                loanId, loan.getLoanStatus(), remaining);

        return LoanReturnResponseDTO.builder()
                .loanId(loanId)
                .loanStatus(loan.getLoanStatus())
                .remainingQuantity(remaining)
                .returnedLoanItemIds(returnedLoanItemIds)
                .returnDate(now)
                .overdue(overdue)
                .build();
    }

    private void releaseEquipmentIfNoUnitsLeft(Equipment equipment, Technician technician, Loan loan) {
        if (equipment.getCurrentStatus() != EquipmentStatus.EMPRESTADO) {
            return;
        }
        if (loanItemRepository.existsByEquipmentIdAndItemStatus(equipment.getId(), ITEM_STATUS_LOANED)) {
            return;
        }

        StatusHistory history = new StatusHistory();
        history.setEquipment(equipment);
        history.setPreviousStatus(equipment.getCurrentStatus().name());
        history.setNewStatus(EquipmentStatus.DISPONIVEL.name());
        history.setChangeDate(LocalDateTime.now());
        history.setTechnician(technician);
        history.setReason("Devolução #" + loan.getId());
        statusHistoryRepository.save(history);

        equipment.setCurrentStatus(EquipmentStatus.DISPONIVEL);
        equipmentRepository.save(equipment);
    }

    // RN01: só empresta equipamento DISPONIVEL (EMPRESTADO, MANUTENCAO, DANIFICADO e INATIVO bloqueiam) e
    // com unidades livres suficientes. Retorna quantas unidades já estão emprestadas.
    private long ensureAvailable(Equipment equipment, long requestedUnits) {
        if (equipment.getCurrentStatus() != EquipmentStatus.DISPONIVEL) {
            throw new EquipmentNotAvailableException(
                    "Equipamento " + equipment.getId() + " indisponível para empréstimo (status "
                            + equipment.getCurrentStatus() + ")");
        }

        long loaned = loanItemRepository
                .countByEquipmentIdAndItemStatus(equipment.getId(), ITEM_STATUS_LOANED);
        long free = equipment.getQuantity() - loaned;
        if (requestedUnits > free) {
            throw new EquipmentNotAvailableException(
                    "Equipamento " + equipment.getId() + " tem apenas " + Math.max(free, 0)
                            + " unidade(s) disponível(is), foram solicitadas " + requestedUnits);
        }
        return loaned;
    }

    private void ensureStudentCanBorrow(Student student, LocalDateTime now) {
        if (loanRepository.hasOverdueLoan(student, now)) {
            throw new LoanNotAllowedException(
                    "Aluno possui empréstimo em atraso e não pode realizar novo empréstimo");
        }
        if (student.getReliabilityRate().compareTo(MIN_RELIABILITY_RATE) < 0) {
            throw new LoanNotAllowedException(
                    "Aluno com confiabilidade abaixo de 50% não pode realizar novo empréstimo");
        }
    }

    private void markAsLoanedIfNoUnitsLeft(
            Equipment equipment, long loanedAfter, Technician technician, Loan loan) {
        if (loanedAfter < equipment.getQuantity()) {
            return;
        }

        StatusHistory history = new StatusHistory();
        history.setEquipment(equipment);
        history.setPreviousStatus(equipment.getCurrentStatus().name());
        history.setNewStatus(EquipmentStatus.EMPRESTADO.name());
        history.setChangeDate(LocalDateTime.now());
        history.setTechnician(technician);
        history.setReason("Empréstimo #" + loan.getId());
        statusHistoryRepository.save(history);

        equipment.setCurrentStatus(EquipmentStatus.EMPRESTADO);
        equipmentRepository.save(equipment);
    }

    // Empréstimos com vários tipos de equipamento ainda não existem (RF09, previsto para um encontro
    // futuro); até lá cada empréstimo tem um único equipamento, repetido por unidade retirada.
    private LoanSummaryDTO toSummary(Loan loan) {
        List<LoanItem> activeItems = loan.getLoanItems().stream()
                .filter(item -> ITEM_STATUS_LOANED.equals(item.getItemStatus()))
                .toList();
        Equipment equipment = activeItems.isEmpty() ? null : activeItems.get(0).getEquipment();

        return LoanSummaryDTO.builder()
                .loanId(loan.getId())
                .equipmentName(equipment != null ? equipment.getName() : null)
                .quantity(activeItems.size())
                .studentName(loan.getStudent().getName())
                .studentRegistrationNumber(loan.getStudent().getRegistrationNumber())
                .professorName(loan.getResponsibleProfessor().getName())
                .technicianName(loan.getTechnician().getName())
                .checkoutDate(loan.getCheckoutDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .loanStatus(loan.getLoanStatus())
                .build();
    }

    private LoanResponseDTO toResponse(Loan loan, Student student, Professor professor, List<LoanItem> items) {
        return LoanResponseDTO.builder()
                .loanId(loan.getId())
                .studentId(student.getId())
                .studentName(student.getName())
                .studentRegistrationNumber(student.getRegistrationNumber())
                .professorId(professor.getId())
                .professorName(professor.getName())
                .checkoutDate(loan.getCheckoutDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .loanStatus(loan.getLoanStatus())
                .items(items.stream()
                        .map(item -> ActiveLoanItemDTO.builder()
                                .loanItemId(item.getId())
                                .equipmentId(item.getEquipment().getId())
                                .equipmentName(item.getEquipment().getName())
                                .checkoutPhoto(item.getCheckoutPhoto())
                                .checkoutCondition(item.getCheckoutCondition())
                                .itemStatus(item.getItemStatus())
                                .build())
                        .toList())
                .build();
    }
}
