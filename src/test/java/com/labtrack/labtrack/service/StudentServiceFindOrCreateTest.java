package com.labtrack.labtrack.service;

import com.labtrack.labtrack.model.Student;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceFindOrCreateTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private StudentService studentService;

    private Student existing(String email, String phone) {
        Student student = new Student();
        student.setId(1L);
        student.setRegistrationNumber("2021001");
        student.setName("João Silva");
        student.setEmail(email);
        student.setPhone(phone);
        student.setReliabilityRate(new BigDecimal("100.00"));
        student.setRegistrationDate(LocalDateTime.now());
        return student;
    }

    @Test
    void createsStudentWhenRegistrationNumberDoesNotExist() {
        Student created = existing("novo@email.com", "1188888888");
        when(studentRepository.findByRegistrationNumber("2021001"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(created));

        Student result = studentService.findOrCreateByRegistrationNumber(
                "2021001", "João Silva", "novo@email.com", "1188888888");

        assertThat(result).isSameAs(created);
        verify(studentRepository).insertIfAbsent("2021001", "João Silva", "novo@email.com", "1188888888");
    }

    @Test
    void returnsExistingStudentWithoutOverwritingCompleteData() {
        Student student = existing("joao@email.com", "1199999999");
        when(studentRepository.findByRegistrationNumber("2021001")).thenReturn(Optional.of(student));

        Student result = studentService.findOrCreateByRegistrationNumber(
                "2021001", "Outro Nome", "outro@email.com", "1100000000");

        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        assertThat(result.getPhone()).isEqualTo("1199999999");
        verify(studentRepository, never()).insertIfAbsent(any(), any(), any(), any());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void completesMissingPhoneOfExistingStudent() {
        Student student = existing("joao@email.com", null);
        when(studentRepository.findByRegistrationNumber("2021001")).thenReturn(Optional.of(student));

        Student result = studentService.findOrCreateByRegistrationNumber(
                "2021001", "João Silva", "outro@email.com", "1100000000");

        assertThat(result.getPhone()).isEqualTo("1100000000");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        verify(studentRepository).save(student);
    }

    @Test
    void completesMissingEmailOfExistingStudent() {
        Student student = existing(" ", "1199999999");
        when(studentRepository.findByRegistrationNumber("2021001")).thenReturn(Optional.of(student));

        Student result = studentService.findOrCreateByRegistrationNumber(
                "2021001", "João Silva", "joao@email.com", null);

        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        assertThat(result.getPhone()).isEqualTo("1199999999");
    }

    @Test
    void createsNewStudentEveryTimeWhenRegistrationNumberIsBlank() {
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        Student result = studentService.findOrCreateByRegistrationNumber(
                "  ", "Visitante", "visitante@email.com", "1177777777");

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertThat(result).isSameAs(captor.getValue());
        assertThat(result.getRegistrationNumber()).isNull();
        assertThat(result.getName()).isEqualTo("Visitante");
        assertThat(result.getReliabilityRate()).isEqualByComparingTo("100");
        verify(studentRepository, never()).findByRegistrationNumber(any());
    }
}
