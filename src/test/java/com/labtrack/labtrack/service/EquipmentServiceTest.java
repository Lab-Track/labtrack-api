package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.EquipmentHistoryDTO;
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
        equipment.setCurrentStatus("available");

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