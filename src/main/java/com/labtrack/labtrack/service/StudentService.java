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

    @Transactional(readOnly = true)
    public StudentResponse findByRegistrationNumber(String registration) {
        log.info("Buscando aluno pela matrícula: {}", registration);

        Student student = studentRepository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new StudentNotFoundException(registration));

        return convertToStudentResponse(student);
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

    /**
     * Busca o aluno pela matrícula e o cria na primeira vez (RF12, RF14, RF15, RN06).
     * A matrícula só identifica o aluno no nosso banco; nome, e-mail e telefone vêm do técnico.
     * Aluno já cadastrado não é sobrescrito: só e-mail/telefone que estiverem faltando são completados.
     * Sem matrícula não há chave de busca, então um novo aluno (sem matrícula) é criado.
     */
    @Transactional
    public Student findOrCreateByRegistrationNumber(
            String registrationNumber, String name, String email, String phone) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            log.info("Cadastrando aluno sem matrícula");
            return studentRepository.save(newStudent(name, email, phone));
        }

        Student student = studentRepository.findByRegistrationNumber(registrationNumber).orElse(null);
        if (student == null) {
            log.info("Matrícula {} nova; cadastrando aluno", registrationNumber);
            studentRepository.insertIfAbsent(registrationNumber, name, email, phone);
            student = studentRepository.findByRegistrationNumber(registrationNumber)
                    .orElseThrow(() -> new IllegalStateException(
                            "Aluno não encontrado após cadastro: " + registrationNumber));
        }

        if (completeMissingData(student, email, phone)) {
            log.info("Dados faltantes do aluno {} completados", registrationNumber);
            studentRepository.save(student);
        }
        return student;
    }

    private boolean completeMissingData(Student student, String email, String phone) {
        boolean changed = false;
        if (isBlank(student.getEmail()) && !isBlank(email)) {
            student.setEmail(email);
            changed = true;
        }
        if (isBlank(student.getPhone()) && !isBlank(phone)) {
            student.setPhone(phone);
            changed = true;
        }
        return changed;
    }

    private Student newStudent(String name, String email, String phone) {
        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPhone(phone);
        student.setReliabilityRate(BigDecimal.valueOf(100));
        student.setRegistrationDate(LocalDateTime.now());
        return student;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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