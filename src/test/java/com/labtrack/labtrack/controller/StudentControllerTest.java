package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ActiveLoanDTO;
import com.labtrack.labtrack.dto.ActiveLoanItemDTO;
import com.labtrack.labtrack.exception.StudentNotFoundException;
import com.labtrack.labtrack.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;  // ← ADICIONAR ESTE IMPORT
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @Test
    void shouldReturn200_WhenStudentHasActiveLoans() {
        // Arrange
        String registration = "2021001";
        ActiveLoanDTO loanDTO = ActiveLoanDTO.builder()
                .loanId(1L)
                .checkoutDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .loanStatus("in_progress")
                .items(List.of(
                        ActiveLoanItemDTO.builder()
                                .loanItemId(1L)
                                .equipmentId(1L)
                                .equipmentName("Multímetro Digital")
                                .checkoutPhoto("checkout.jpg")
                                .checkoutCondition("GOOD")
                                .itemStatus("loaned")
                                .build()
                ))
                .build();

        when(studentService.findActiveLoansByRegistration(registration))
                .thenReturn(List.of(loanDTO));

        // Act
        ResponseEntity<List<ActiveLoanDTO>> response = studentController
                .getActiveLoans(registration);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getLoanId()).isEqualTo(1L);
        assertThat(response.getBody().get(0).getLoanStatus()).isEqualTo("in_progress");
    }

    @Test
    void shouldReturn200WithEmptyList_WhenStudentHasNoActiveLoans() {
        // Arrange
        String registration = "2021001";
        when(studentService.findActiveLoansByRegistration(registration))
                .thenReturn(List.of());

        // Act
        ResponseEntity<List<ActiveLoanDTO>> response = studentController
                .getActiveLoans(registration);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldThrowStudentNotFoundException_WhenStudentNotFound() {
        // Arrange
        String registration = "9999999";
        when(studentService.findActiveLoansByRegistration(registration))
                .thenThrow(new StudentNotFoundException(registration));

        // Act & Assert
        assertThatThrownBy(() -> studentController.getActiveLoans(registration))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("Aluno não encontrado com matrícula: " + registration);
    }
}