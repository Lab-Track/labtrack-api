package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
import com.labtrack.labtrack.dto.EquipmentRequestDTO;
import com.labtrack.labtrack.dto.EquipmentResponseDTO;
import com.labtrack.labtrack.exception.DuplicateEquipmentCodeException;
import com.labtrack.labtrack.exception.EquipmentNotFoundException;
import com.labtrack.labtrack.model.*;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private LoanItemRepository loanItemRepository;

    @Mock
    private LoanReturnRepository loanReturnRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment equipment;
    private Professor professor;
    private Student studentA;
    private Student studentB;
    private Loan loanA;
    private Loan loanB;
    private LoanItem loanItemA;
    private LoanItem loanItemB;
    private LoanReturn loanReturnA;

    @BeforeEach
    void setUp() {
        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Osciloscópio");
        equipment.setIdentificationPhoto("osciloscopio.jpg");
        equipment.setCurrentStatus(EquipmentStatus.DISPONIVEL);

        professor = new Professor();
        professor.setId(1L);
        professor.setName("Carlos Lima");
        professor.setEmail("carlos.lima@labtrack.local");

        studentA = new Student();
        studentA.setId(1L);
        studentA.setName("Ana Souza");

        studentB = new Student();
        studentB.setId(2L);
        studentB.setName("Bruno Lima");

        loanA = new Loan();
        loanA.setId(10L);
        loanA.setStudent(studentA);
        loanA.setResponsibleProfessor(professor);
        loanA.setCheckoutDate(LocalDateTime.of(2026, 9, 1, 10, 0));
        loanA.setExpectedReturnDate(LocalDateTime.of(2026, 9, 8, 10, 0));
        loanA.setLoanStatus("returned");

        loanItemA = new LoanItem();
        loanItemA.setId(100L);
        loanItemA.setLoan(loanA);
        loanItemA.setEquipment(equipment);
        loanItemA.setItemStatus("returned");

        loanReturnA = new LoanReturn();
        loanReturnA.setId(1000L);
        loanReturnA.setLoanItem(loanItemA);
        loanReturnA.setReturnDate(LocalDateTime.of(2026, 9, 2, 9, 0));
        loanReturnA.setReturnCondition("GOOD");
        loanReturnA.setVerificationStatus("working");
        loanReturnA.setOverdue(false);

        loanB = new Loan();
        loanB.setId(11L);
        loanB.setStudent(studentB);
        loanB.setResponsibleProfessor(professor);
        loanB.setCheckoutDate(LocalDateTime.of(2026, 9, 5, 14, 0));
        loanB.setExpectedReturnDate(LocalDateTime.of(2026, 9, 12, 14, 0));
        loanB.setLoanStatus("in_progress");

        loanItemB = new LoanItem();
        loanItemB.setId(101L);
        loanItemB.setLoan(loanB);
        loanItemB.setEquipment(equipment);
        loanItemB.setItemStatus("loaned");
    }

    @Test
    void shouldCreateEquipment_WhenValidRequest() {
        // Arrange
        EquipmentRequestDTO request = buildCreateRequest(EquipmentStatus.MANUTENCAO);

        when(equipmentRepository.existsByCode("EQP-0001")).thenReturn(false);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> {
            Equipment saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.of(2026, 9, 19, 10, 30));
            return saved;
        });

        // Act
        EquipmentResponseDTO response = equipmentService.createEquipment(request);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Multímetro Digital");
        assertThat(response.getCode()).isEqualTo("EQP-0001");
        assertThat(response.getIdentificationPhoto()).isEqualTo("foto.jpg");
        assertThat(response.getCurrentStatus()).isEqualTo(EquipmentStatus.MANUTENCAO);
        assertThat(response.getCategory()).isEqualTo("Medição");
        assertThat(response.getLaboratory()).isEqualTo("Laboratório de Eletrônica");
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 9, 19, 10, 30));
    }

    @Test
    void shouldDefaultStatusToDisponivel_WhenRequestHasNoStatus() {
        // Arrange
        EquipmentRequestDTO request = buildCreateRequest(null);

        when(equipmentRepository.existsByCode("EQP-0001")).thenReturn(false);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EquipmentResponseDTO response = equipmentService.createEquipment(request);

        // Assert
        assertThat(response.getCurrentStatus()).isEqualTo(EquipmentStatus.DISPONIVEL);
    }

    @Test
    void shouldThrowDuplicateEquipmentCodeException_WhenCodeAlreadyExists() {
        // Arrange
        when(equipmentRepository.existsByCode("EQP-0001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> equipmentService.createEquipment(buildCreateRequest(null)))
                .isInstanceOf(DuplicateEquipmentCodeException.class)
                .hasMessageContaining("EQP-0001");

        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void shouldSubtractLoanedItemsFromAvailableQuantity() {
        // Arrange
        EquipmentRequestDTO request = buildCreateRequest(null);

        when(equipmentRepository.existsByCode("EQP-0001")).thenReturn(false);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> {
            Equipment saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });
        when(loanItemRepository.countByEquipmentIdAndItemStatus(7L, "loaned")).thenReturn(2L);

        // Act
        EquipmentResponseDTO response = equipmentService.createEquipment(request);

        // Assert
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getAvailableQuantity()).isEqualTo(1);
    }

    private EquipmentRequestDTO buildCreateRequest(EquipmentStatus status) {
        return EquipmentRequestDTO.builder()
                .name("Multímetro Digital")
                .code("EQP-0001")
                .identificationPhoto("foto.jpg")
                .currentStatus(status)
                .category("Medição")
                .laboratory("Laboratório de Eletrônica")
                .quantity(3)
                .build();
    }

    @Test
    void shouldReturnEventsOrderedByDateDescendingAndPaginated_WhenEquipmentHasLoans() {
        // Arrange
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(loanItemRepository.findByEquipmentId(1L)).thenReturn(List.of(loanItemA, loanItemB));
        when(loanReturnRepository.findByEquipmentId(1L)).thenReturn(List.of(loanReturnA));

        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<EquipmentHistoryDTO> result = equipmentService.findLoanHistoryByEquipmentId(1L, pageable);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        EquipmentHistoryDTO first = result.getContent().get(0);
        assertThat(first.getEventType()).isEqualTo("RETIRADA");
        assertThat(first.getLoanId()).isEqualTo(11L);
        assertThat(first.getStudentName()).isEqualTo("Bruno Lima");
        assertThat(first.getProfessorName()).isEqualTo("Carlos Lima");
        assertThat(first.getEventDate()).isEqualTo(LocalDateTime.of(2026, 9, 5, 14, 0));

        EquipmentHistoryDTO second = result.getContent().get(1);
        assertThat(second.getEventType()).isEqualTo("DEVOLUCAO");
        assertThat(second.getLoanId()).isEqualTo(10L);
        assertThat(second.getStudentName()).isEqualTo("Ana Souza");
        assertThat(second.getEventDate()).isEqualTo(LocalDateTime.of(2026, 9, 2, 9, 0));
    }

    @Test
    void shouldReturnSecondPage_WithRemainingEvent() {
        // Arrange
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(loanItemRepository.findByEquipmentId(1L)).thenReturn(List.of(loanItemA, loanItemB));
        when(loanReturnRepository.findByEquipmentId(1L)).thenReturn(List.of(loanReturnA));

        Pageable pageable = PageRequest.of(1, 2);

        // Act
        Page<EquipmentHistoryDTO> result = equipmentService.findLoanHistoryByEquipmentId(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEventType()).isEqualTo("RETIRADA");
        assertThat(result.getContent().get(0).getLoanId()).isEqualTo(10L);
    }

    @Test
    void shouldReturnEmptyPage_WithoutOverflow_WhenOffsetExceedsIntRange() {
        // Arrange
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(loanItemRepository.findByEquipmentId(1L)).thenReturn(List.of(loanItemA, loanItemB));
        when(loanReturnRepository.findByEquipmentId(1L)).thenReturn(List.of(loanReturnA));

        // offset = 500_000 * 5_000 = 2_500_000_000, que estoura pra negativo se
        // for truncado direto pra int (2_500_000_000 - 2^32 = -1_794_967_296)
        Pageable pageable = PageRequest.of(500_000, 5_000);

        // Act
        Page<EquipmentHistoryDTO> result = equipmentService.findLoanHistoryByEquipmentId(1L, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyPage_WhenEquipmentHasNoLoans() {
        // Arrange
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(loanItemRepository.findByEquipmentId(1L)).thenReturn(List.of());
        when(loanReturnRepository.findByEquipmentId(1L)).thenReturn(List.of());

        // Act
        Page<EquipmentHistoryDTO> result = equipmentService
                .findLoanHistoryByEquipmentId(1L, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldThrowEquipmentNotFoundException_WhenEquipmentDoesNotExist() {
        // Arrange
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> equipmentService.findLoanHistoryByEquipmentId(1L, PageRequest.of(0, 10)))
                .isInstanceOf(EquipmentNotFoundException.class)
                .hasMessageContaining("Equipamento não encontrado com id: 1");

        verify(loanItemRepository, never()).findByEquipmentId(any());
        verify(loanReturnRepository, never()).findByEquipmentId(any());
    }
}
