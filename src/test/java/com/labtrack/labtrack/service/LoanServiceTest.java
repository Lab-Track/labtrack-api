package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.LoanItemRequestDTO;
import com.labtrack.labtrack.dto.LoanRequestDTO;
import com.labtrack.labtrack.dto.LoanResponseDTO;
import com.labtrack.labtrack.dto.LoanSummaryDTO;
import com.labtrack.labtrack.exception.EquipmentNotAvailableException;
import com.labtrack.labtrack.exception.EquipmentNotFoundException;
import com.labtrack.labtrack.exception.LoanNotAllowedException;
import com.labtrack.labtrack.exception.ProfessorNotFoundException;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.LoanItem;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.StatusHistory;
import com.labtrack.labtrack.model.Student;
import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.ProfessorRepository;
import com.labtrack.labtrack.repository.StatusHistoryRepository;
import com.labtrack.labtrack.repository.TechnicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanItemRepository loanItemRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private ProfessorRepository professorRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private StatusHistoryRepository statusHistoryRepository;
    @Mock
    private StudentService studentService;

    @InjectMocks
    private LoanService loanService;

    private Professor professor;
    private Technician technician;
    private Student student;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        professor = new Professor();
        professor.setId(10L);
        professor.setName("Carlos Lima");

        technician = new Technician();
        technician.setId(5L);
        technician.setLogin("tecnico.teste");
        technician.setName("Técnico");

        student = new Student();
        student.setId(7L);
        student.setName("Ana Souza");
        student.setRegistrationNumber("2021001");
        student.setReliabilityRate(new BigDecimal("100.00"));

        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Osciloscópio");
        equipment.setQuantity(1);
        equipment.setCurrentStatus(EquipmentStatus.DISPONIVEL);
    }

    private LoanRequestDTO request(LoanItemRequestDTO... items) {
        return LoanRequestDTO.builder()
                .studentRegistrationNumber("2021001")
                .studentName("Ana Souza")
                .studentEmail("ana@email.com")
                .studentPhone("1199999999")
                .professorId(10L)
                .items(List.of(items))
                .build();
    }

    private LoanItemRequestDTO item(Long equipmentId) {
        return LoanItemRequestDTO.builder()
                .equipmentId(equipmentId)
                .checkoutPhoto("uploads/foto-" + equipmentId + ".jpg")
                .checkoutCondition("Sem avarias")
                .build();
    }

    private void stubHappyPath() {
        when(professorRepository.findById(10L)).thenReturn(Optional.of(professor));
        when(technicianRepository.findByLogin("tecnico.teste")).thenReturn(Optional.of(technician));
        when(equipmentRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(equipment));
        when(loanItemRepository.countByEquipmentIdAndItemStatus(anyLong(), eq("loaned"))).thenReturn(0L);
        when(studentService.findOrCreateByRegistrationNumber(any(), any(), any(), any())).thenReturn(student);
        lenient().when(loanRepository.hasOverdueLoan(eq(student), any(LocalDateTime.class))).thenReturn(false);
        lenient().when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan loan = inv.getArgument(0);
            loan.setId(100L);
            return loan;
        });
        lenient().when(loanItemRepository.save(any(LoanItem.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsLoanWithOneItemPerEquipmentAndMarksEquipmentAsLoaned() {
        stubHappyPath();

        LoanResponseDTO response = loanService.createLoan(request(item(1L)), "tecnico.teste");

        assertThat(response.getLoanId()).isEqualTo(100L);
        assertThat(response.getLoanStatus()).isEqualTo("in_progress");
        assertThat(response.getStudentId()).isEqualTo(7L);
        assertThat(response.getProfessorId()).isEqualTo(10L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getItemStatus()).isEqualTo("loaned");
        assertThat(response.getItems().get(0).getCheckoutPhoto()).isEqualTo("uploads/foto-1.jpg");

        assertThat(equipment.getCurrentStatus()).isEqualTo(EquipmentStatus.EMPRESTADO);
        ArgumentCaptor<StatusHistory> history = ArgumentCaptor.forClass(StatusHistory.class);
        verify(statusHistoryRepository).save(history.capture());
        assertThat(history.getValue().getPreviousStatus()).isEqualTo("DISPONIVEL");
        assertThat(history.getValue().getNewStatus()).isEqualTo("EMPRESTADO");
        assertThat(history.getValue().getTechnician()).isSameAs(technician);
    }

    @Test
    void defaultsExpectedReturnDateToSevenDaysWhenNotInformed() {
        stubHappyPath();

        LoanResponseDTO response = loanService.createLoan(request(item(1L)), "tecnico.teste");

        assertThat(response.getExpectedReturnDate())
                .isBetween(LocalDateTime.now().plusDays(7).minusMinutes(1),
                        LocalDateTime.now().plusDays(7).plusMinutes(1));
    }

    @Test
    void usesExpectedReturnDateWhenInformed() {
        stubHappyPath();
        LocalDateTime expected = LocalDateTime.now().plusDays(3);
        LoanRequestDTO request = request(item(1L));
        request.setExpectedReturnDate(expected);

        LoanResponseDTO response = loanService.createLoan(request, "tecnico.teste");

        assertThat(response.getExpectedReturnDate()).isEqualTo(expected);
    }

    @Test
    void keepsEquipmentAvailableWhileThereAreFreeUnits() {
        equipment.setQuantity(3);
        stubHappyPath();

        loanService.createLoan(request(item(1L)), "tecnico.teste");

        assertThat(equipment.getCurrentStatus()).isEqualTo(EquipmentStatus.DISPONIVEL);
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    void rejectsLoanWhenEquipmentIsAlreadyLoaned() {
        equipment.setCurrentStatus(EquipmentStatus.EMPRESTADO);
        when(professorRepository.findById(10L)).thenReturn(Optional.of(professor));
        when(technicianRepository.findByLogin("tecnico.teste")).thenReturn(Optional.of(technician));
        when(equipmentRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(equipment));

        assertThatThrownBy(() -> loanService.createLoan(request(item(1L)), "tecnico.teste"))
                .isInstanceOf(EquipmentNotAvailableException.class);

        verify(loanRepository, never()).save(any());
        verify(studentService, never()).findOrCreateByRegistrationNumber(any(), any(), any(), any());
    }

    @Test
    void rejectsLoanWhenRequestedUnitsExceedAvailableUnits() {
        equipment.setQuantity(2);
        when(professorRepository.findById(10L)).thenReturn(Optional.of(professor));
        when(technicianRepository.findByLogin("tecnico.teste")).thenReturn(Optional.of(technician));
        when(equipmentRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(equipment));
        when(loanItemRepository.countByEquipmentIdAndItemStatus(1L, "loaned")).thenReturn(1L);

        assertThatThrownBy(() -> loanService.createLoan(request(item(1L), item(1L)), "tecnico.teste"))
                .isInstanceOf(EquipmentNotAvailableException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void rejectsLoanWhenEquipmentDoesNotExist() {
        when(professorRepository.findById(10L)).thenReturn(Optional.of(professor));
        when(technicianRepository.findByLogin("tecnico.teste")).thenReturn(Optional.of(technician));
        when(equipmentRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of());

        assertThatThrownBy(() -> loanService.createLoan(request(item(1L)), "tecnico.teste"))
                .isInstanceOf(EquipmentNotFoundException.class);
    }

    @Test
    void rejectsLoanWhenProfessorDoesNotExist() {
        when(professorRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.createLoan(request(item(1L)), "tecnico.teste"))
                .isInstanceOf(ProfessorNotFoundException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void rejectsLoanWhenStudentHasOverdueLoan() {
        stubHappyPath();
        when(loanRepository.hasOverdueLoan(eq(student), any(LocalDateTime.class))).thenReturn(true);

        assertThatThrownBy(() -> loanService.createLoan(request(item(1L)), "tecnico.teste"))
                .isInstanceOf(LoanNotAllowedException.class)
                .hasMessageContaining("atraso");

        verify(loanRepository, never()).save(any());
        assertThat(equipment.getCurrentStatus()).isEqualTo(EquipmentStatus.DISPONIVEL);
    }

    @Test
    void rejectsLoanWhenStudentReliabilityIsBelowFiftyPercent() {
        student.setReliabilityRate(new BigDecimal("49.99"));
        stubHappyPath();

        assertThatThrownBy(() -> loanService.createLoan(request(item(1L)), "tecnico.teste"))
                .isInstanceOf(LoanNotAllowedException.class)
                .hasMessageContaining("confiabilidade");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void allowsLoanWhenStudentReliabilityIsExactlyFiftyPercent() {
        student.setReliabilityRate(new BigDecimal("50.00"));
        stubHappyPath();

        LoanResponseDTO response = loanService.createLoan(request(item(1L)), "tecnico.teste");

        assertThat(response.getLoanId()).isEqualTo(100L);
    }

    @Test
    void listsActiveLoansWithEquipmentNameAndQuantityFromLoanedItems() {
        Loan loan = new Loan();
        loan.setId(200L);
        loan.setStudent(student);
        loan.setResponsibleProfessor(professor);
        loan.setTechnician(technician);
        loan.setCheckoutDate(LocalDateTime.now().minusDays(1));
        loan.setExpectedReturnDate(LocalDateTime.now().plusDays(6));
        loan.setLoanStatus("in_progress");

        LoanItem loanedItem = new LoanItem();
        loanedItem.setEquipment(equipment);
        loanedItem.setItemStatus("loaned");

        LoanItem returnedItem = new LoanItem();
        returnedItem.setEquipment(equipment);
        returnedItem.setItemStatus("returned");

        loan.setLoanItems(List.of(loanedItem, returnedItem));

        when(loanRepository.findAllActiveOrderByCheckoutDateDesc()).thenReturn(List.of(loan));

        List<LoanSummaryDTO> result = loanService.findAllActiveLoans();

        assertThat(result).hasSize(1);
        LoanSummaryDTO summary = result.get(0);
        assertThat(summary.getLoanId()).isEqualTo(200L);
        assertThat(summary.getEquipmentName()).isEqualTo("Osciloscópio");
        assertThat(summary.getQuantity()).isEqualTo(1);
        assertThat(summary.getStudentName()).isEqualTo("Ana Souza");
        assertThat(summary.getStudentRegistrationNumber()).isEqualTo("2021001");
        assertThat(summary.getProfessorName()).isEqualTo("Carlos Lima");
        assertThat(summary.getTechnicianName()).isEqualTo("Técnico");
        assertThat(summary.getLoanStatus()).isEqualTo("in_progress");
    }

    @Test
    void listsActiveLoanWithNullEquipmentNameWhenAllItemsAlreadyReturned() {
        Loan loan = new Loan();
        loan.setId(201L);
        loan.setStudent(student);
        loan.setResponsibleProfessor(professor);
        loan.setTechnician(technician);
        loan.setCheckoutDate(LocalDateTime.now());
        loan.setExpectedReturnDate(LocalDateTime.now().plusDays(7));
        loan.setLoanStatus("partial");

        LoanItem returnedItem = new LoanItem();
        returnedItem.setEquipment(equipment);
        returnedItem.setItemStatus("returned");
        loan.setLoanItems(List.of(returnedItem));

        when(loanRepository.findAllActiveOrderByCheckoutDateDesc()).thenReturn(List.of(loan));

        List<LoanSummaryDTO> result = loanService.findAllActiveLoans();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEquipmentName()).isNull();
        assertThat(result.get(0).getQuantity()).isZero();
    }

    @Test
    void returnsEmptyListWhenThereAreNoActiveLoans() {
        when(loanRepository.findAllActiveOrderByCheckoutDateDesc()).thenReturn(List.of());

        List<LoanSummaryDTO> result = loanService.findAllActiveLoans();

        assertThat(result).isEmpty();
    }

    @Test
    void marksOverdueLoansUsingCurrentTime() {
        when(loanRepository.markOverdueLoans(any(LocalDateTime.class))).thenReturn(2);

        int updated = loanService.markOverdueLoans();

        assertThat(updated).isEqualTo(2);
    }
}
