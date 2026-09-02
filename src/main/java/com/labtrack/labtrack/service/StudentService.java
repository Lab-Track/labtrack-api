package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.ActiveLoanDTO;
import com.labtrack.labtrack.dto.ActiveLoanItemDTO;
import com.labtrack.labtrack.dto.StudentCreateRequest;
import com.labtrack.labtrack.dto.StudentResponse;
import com.labtrack.labtrack.exception.DuplicateRegistrationNumberException;
import com.labtrack.labtrack.exception.StudentNotFoundException;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.LoanItem;
import com.labtrack.labtrack.model.Student;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final LoanRepository loanRepository;

    @Transactional(readOnly = true)
    public List<ActiveLoanDTO> findActiveLoansByRegistration(String registration) {
        log.info("Buscando empréstimos ativos para matrícula: {}", registration);

        Student student = studentRepository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new StudentNotFoundException(registration));

        List<Loan> activeLoans = loanRepository.findActiveLoansByStudent(student);

        log.info("Encontrados {} empréstimos ativos para matrícula: {}", activeLoans.size(), registration);

        return activeLoans.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        log.info("Cadastrando aluno com matrícula: {}", request.registrationNumber());

        studentRepository.findByRegistrationNumber(request.registrationNumber())
                .ifPresent(existing -> {
                    throw new DuplicateRegistrationNumberException(request.registrationNumber());
                });

        Student student = new Student();
        student.setName(request.name());
        student.setRegistrationNumber(request.registrationNumber());
        student.setEmail(request.email());
        student.setPhone(request.phone());
        student.setReliabilityRate(BigDecimal.valueOf(100));
        student.setRegistrationDate(LocalDateTime.now());

        Student saved = studentRepository.save(student);

        log.info("Aluno cadastrado com id: {}", saved.getId());

        return convertToStudentResponse(saved);
    }

    private StudentResponse convertToStudentResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getRegistrationNumber(),
                student.getEmail(),
                student.getPhone(),
                student.getReliabilityRate(),
                student.getRegistrationDate());
    }

    private ActiveLoanDTO convertToDTO(Loan loan) {
        return ActiveLoanDTO.builder()
                .loanId(loan.getId())
                .checkoutDate(loan.getCheckoutDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .extendedDate(loan.getExtendedDate())
                .loanStatus(loan.getLoanStatus())
                .items(convertLoanItemsToDTO(loan.getLoanItems()))
                .build();
    }

    private List<ActiveLoanItemDTO> convertLoanItemsToDTO(List<LoanItem> loanItems) {
        if (loanItems == null || loanItems.isEmpty()) {
            return List.of();
        }

        return loanItems.stream()
                .map(item -> ActiveLoanItemDTO.builder()
                        .loanItemId(item.getId())
                        .equipmentId(item.getEquipment().getId())
                        .equipmentName(item.getEquipment().getName())
                        .checkoutPhoto(item.getCheckoutPhoto())
                        .checkoutCondition(item.getCheckoutCondition())
                        .itemStatus(item.getItemStatus())
                        .build())
                .toList();
    }
}