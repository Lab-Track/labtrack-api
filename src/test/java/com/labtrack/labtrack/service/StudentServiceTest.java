package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.ActiveLoanDTO;
import com.labtrack.labtrack.dto.ActiveLoanItemDTO;
import com.labtrack.labtrack.dto.StudentCreateRequest;
import com.labtrack.labtrack.dto.StudentResponse;
import com.labtrack.labtrack.exception.DuplicateRegistrationNumberException;
import com.labtrack.labtrack.exception.StudentNotFoundException;
import com.labtrack.labtrack.model.*;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;
    private Loan loan;
    private LoanItem loanItem;
    private Equipment equipment;
    private String registration;

    @BeforeEach
    void setUp() {
        registration = "2021001";

        student = new Student();
        student.setId(1L);
        student.setRegistrationNumber(registration);
        student.setName("João Silva");
        student.setEmail("joao@email.com");
        student.setPhone("1199999999");
        student.setReliabilityRate(new BigDecimal("100.00"));
        student.setRegistrationDate(LocalDateTime.now());

        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Multímetro Digital");
        equipment.setIdentificationPhoto("multimetro.jpg");
        equipment.setCurrentStatus(EquipmentStatus.DISPONIVEL);

        loan = new Loan();
        loan.setId(1L);
        loan.setStudent(student);
        loan.setCheckoutDate(LocalDateTime.now());
        loan.setExpectedReturnDate(LocalDateTime.now().plusDays(7));
        loan.setLoanStatus("in_progress");

        loanItem = new LoanItem();
        loanItem.setId(1L);
        loanItem.setLoan(loan);
        loanItem.setEquipment(equipment);
        loanItem.setCheckoutPhoto("checkout_photo.jpg");
        loanItem.setCheckoutCondition("GOOD");
        loanItem.setItemStatus("loaned");

        loan.setLoanItems(List.of(loanItem));
    }

    @Test
    void shouldReturnActiveLoans_WhenStudentExists() {
        // Arrange
        when(studentRepository.findByRegistrationNumber(registration))
                .thenReturn(Optional.of(student));
        when(loanRepository.findActiveLoansByStudent(student))
                .thenReturn(List.of(loan));

        // Act
        List<ActiveLoanDTO> result = studentService.findActiveLoansByRegistration(registration);

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(1);

        ActiveLoanDTO loanDTO = result.get(0);
        assertThat(loanDTO.getLoanId()).isEqualTo(1L);
        assertThat(loanDTO.getLoanStatus()).isEqualTo("in_progress");
        assertThat(loanDTO.getItems()).hasSize(1);

        ActiveLoanItemDTO itemDTO = loanDTO.getItems().get(0);
        assertThat(itemDTO.getEquipmentId()).isEqualTo(1L);
        assertThat(itemDTO.getEquipmentName()).isEqualTo("Multímetro Digital");
        assertThat(itemDTO.getItemStatus()).isEqualTo("loaned");

        verify(studentRepository).findByRegistrationNumber(registration);
        verify(loanRepository).findActiveLoansByStudent(student);
    }

    @Test
    void shouldReturnEmptyList_WhenStudentHasNoActiveLoans() {
        // Arrange
        when(studentRepository.findByRegistrationNumber(registration))
                .thenReturn(Optional.of(student));
        when(loanRepository.findActiveLoansByStudent(student))
                .thenReturn(List.of());

        // Act
        List<ActiveLoanDTO> result = studentService.findActiveLoansByRegistration(registration);

        // Assert
        assertThat(result).isNotNull().isEmpty();
        verify(studentRepository).findByRegistrationNumber(registration);
        verify(loanRepository).findActiveLoansByStudent(student);
    }

    @Test
    void shouldThrowStudentNotFoundException_WhenStudentDoesNotExist() {
        // Arrange
        when(studentRepository.findByRegistrationNumber(registration))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.findActiveLoansByRegistration(registration))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("Aluno não encontrado com matrícula: " + registration);

        verify(studentRepository).findByRegistrationNumber(registration);
        verify(loanRepository, never()).findActiveLoansByStudent(any());
    }

    @Test
    void shouldCreateStudent_WhenRegistrationNumberIsNew() {
        // Arrange
        String newRegistration = "2022050";
        StudentCreateRequest request = new StudentCreateRequest(
                "Maria Souza", newRegistration, "maria@email.com", "1188888888");

        when(studentRepository.findByRegistrationNumber(newRegistration))
                .thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> {
                    Student saved = invocation.getArgument(0);
                    saved.setId(2L);
                    return saved;
                });

        // Act
        StudentResponse result = studentService.createStudent(request);

        // Assert
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Maria Souza");
        assertThat(result.registrationNumber()).isEqualTo(newRegistration);
        assertThat(result.email()).isEqualTo("maria@email.com");
        assertThat(result.phone()).isEqualTo("1188888888");
        assertThat(result.reliabilityRate()).isEqualByComparingTo("100");
        assertThat(result.registrationDate()).isNotNull();

        verify(studentRepository).findByRegistrationNumber(newRegistration);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void shouldThrowDuplicateRegistrationNumberException_WhenRegistrationNumberAlreadyExists() {
        // Arrange
        StudentCreateRequest request = new StudentCreateRequest(
                "Maria Souza", registration, "maria@email.com", "1188888888");

        when(studentRepository.findByRegistrationNumber(registration))
                .thenReturn(Optional.of(student));

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(DuplicateRegistrationNumberException.class)
                .hasMessageContaining(registration);

        verify(studentRepository).findByRegistrationNumber(registration);
        verify(studentRepository, never()).save(any());
    }
}