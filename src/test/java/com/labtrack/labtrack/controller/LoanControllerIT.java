package com.labtrack.labtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.Student;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.ProfessorRepository;
import com.labtrack.labtrack.repository.StatusHistoryRepository;
import com.labtrack.labtrack.repository.StudentRepository;
import com.labtrack.labtrack.repository.TechnicianRepository;
import com.labtrack.labtrack.security.JwtService;
import com.labtrack.labtrack.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TechnicianRepository technicianRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private LoanItemRepository loanItemRepository;
    @Autowired
    private StatusHistoryRepository statusHistoryRepository;
    @Autowired
    private LoanService loanService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Professor professor;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        professor = new Professor();
        professor.setName("Carlos Lima");
        professor.setEmail("carlos.lima@labtrack.local");
        professor = professorRepository.save(professor);

        equipment = newEquipment("LOAN-IT-001", 1);
    }

    private Equipment newEquipment(String code, int quantity) {
        Equipment e = new Equipment();
        e.setName("Osciloscópio");
        e.setIdentificationPhoto("osciloscopio.jpg");
        e.setCode(code);
        e.setCurrentStatus(EquipmentStatus.DISPONIVEL);
        e.setCategory("Medição");
        e.setLaboratory("Laboratório de Eletrônica");
        e.setQuantity(quantity);
        return equipmentRepository.save(e);
    }

    private String validToken() {
        var technician = technicianRepository.findByLogin("tecnico.teste").orElseThrow();
        UserDetails userDetails = User.withUsername(technician.getLogin())
                .password(technician.getPasswordHash())
                .authorities("ROLE_TECHNICIAN")
                .build();
        return jwtService.generateToken(userDetails);
    }

    private Map<String, Object> body(String registration, Long equipmentId, String photo) {
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("equipamentoId", equipmentId);
        item.put("fotoRetirada", photo);
        item.put("estadoRetirada", "Sem avarias");

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("alunoMatricula", registration);
        body.put("alunoNome", "Ana Souza");
        body.put("alunoEmail", "ana@email.com");
        body.put("alunoTelefone", "11999999999");
        body.put("professorId", professor.getId());
        body.put("itens", List.of(item));
        return body;
    }

    private org.springframework.test.web.servlet.ResultActions postLoan(Map<String, Object> body)
            throws Exception {
        return mockMvc.perform(post("/api/loans")
                .header("Authorization", "Bearer " + validToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private Student newStudent(String registration, BigDecimal reliability) {
        Student s = new Student();
        s.setRegistrationNumber(registration);
        s.setName("Aluno Existente");
        s.setEmail("existente@email.com");
        s.setReliabilityRate(reliability);
        s.setRegistrationDate(LocalDateTime.now());
        return studentRepository.save(s);
    }

    private Loan newLoan(Student student, LocalDateTime expectedReturn, String status) {
        var technician = technicianRepository.findByLogin("tecnico.teste").orElseThrow();
        Loan loan = new Loan();
        loan.setStudent(student);
        loan.setResponsibleProfessor(professor);
        loan.setTechnician(technician);
        loan.setCheckoutDate(LocalDateTime.now().minusDays(10));
        loan.setExpectedReturnDate(expectedReturn);
        loan.setLoanStatus(status);
        return loanRepository.save(loan);
    }

    @Test
    void createLoanReturnsCreatedAndMarksEquipmentAsLoaned() throws Exception {
        postLoan(body("LOAN-2021001", equipment.getId(), "/uploads/foto.jpg"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").isNotEmpty())
                .andExpect(jsonPath("$.loanStatus").value("in_progress"))
                .andExpect(jsonPath("$.studentRegistrationNumber").value("LOAN-2021001"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].checkoutPhoto").value("/uploads/foto.jpg"))
                .andExpect(jsonPath("$.items[0].itemStatus").value("loaned"));

        assertThat(equipmentRepository.findById(equipment.getId()).orElseThrow().getCurrentStatus())
                .isEqualTo(EquipmentStatus.EMPRESTADO);
        assertThat(studentRepository.findByRegistrationNumber("LOAN-2021001")).isPresent();
        assertThat(statusHistoryRepository.findAll())
                .anyMatch(h -> h.getEquipment().getId().equals(equipment.getId())
                        && "EMPRESTADO".equals(h.getNewStatus()));
    }

    @Test
    void createLoanReusesExistingStudentWithoutDuplicating() throws Exception {
        Student existing = newStudent("LOAN-2021002", new BigDecimal("100.00"));

        postLoan(body("LOAN-2021002", equipment.getId(), "/uploads/foto.jpg"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(existing.getId()))
                .andExpect(jsonPath("$.studentName").value("Aluno Existente"));

        assertThat(studentRepository.findAll().stream()
                .filter(s -> "LOAN-2021002".equals(s.getRegistrationNumber())).count()).isEqualTo(1);
    }

    @Test
    void createLoanWithoutRegistrationNumberCreatesStudentWithoutRegistration() throws Exception {
        postLoan(body(null, equipment.getId(), "/uploads/foto.jpg"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentRegistrationNumber").doesNotExist());
    }

    @Test
    void createLoanWithAlreadyLoanedEquipmentReturnsConflict() throws Exception {
        equipment.setCurrentStatus(EquipmentStatus.EMPRESTADO);
        equipmentRepository.save(equipment);

        postLoan(body("LOAN-2021003", equipment.getId(), "/uploads/foto.jpg"))
                .andExpect(status().isConflict());

        assertThat(loanRepository.count()).isZero();
        assertThat(studentRepository.findByRegistrationNumber("LOAN-2021003")).isEmpty();
    }

    @Test
    void createLoanWithoutPhotoReturnsBadRequestAndCreatesNothing() throws Exception {
        long loansBefore = loanRepository.count();

        postLoan(body("LOAN-2021004", equipment.getId(), " "))
                .andExpect(status().isBadRequest());

        assertThat(loanRepository.count()).isEqualTo(loansBefore);
        assertThat(studentRepository.findByRegistrationNumber("LOAN-2021004")).isEmpty();
    }

    @Test
    void createLoanForStudentWithOverdueLoanReturnsUnprocessableEntity() throws Exception {
        Student student = newStudent("LOAN-2021005", new BigDecimal("100.00"));
        newLoan(student, LocalDateTime.now().minusDays(1), "in_progress");

        postLoan(body("LOAN-2021005", equipment.getId(), "/uploads/foto.jpg"))
                .andExpect(status().isUnprocessableEntity());

        assertThat(equipmentRepository.findById(equipment.getId()).orElseThrow().getCurrentStatus())
                .isEqualTo(EquipmentStatus.DISPONIVEL);
    }

    @Test
    void createLoanForStudentWithLowReliabilityReturnsUnprocessableEntity() throws Exception {
        newStudent("LOAN-2021006", new BigDecimal("49.00"));

        postLoan(body("LOAN-2021006", equipment.getId(), "/uploads/foto.jpg"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createLoanWithUnknownProfessorReturnsNotFound() throws Exception {
        Map<String, Object> body = body("LOAN-2021007", equipment.getId(), "/uploads/foto.jpg");
        body.put("professorId", 999999L);

        postLoan(body).andExpect(status().isNotFound());
    }

    @Test
    void createLoanWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                body("LOAN-2021008", equipment.getId(), "/uploads/foto.jpg"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void overdueJobMarksOpenLoansPastDueAsLate() {
        Student student = newStudent("LOAN-2021009", new BigDecimal("100.00"));
        Loan overdue = newLoan(student, LocalDateTime.now().minusDays(1), "in_progress");
        Loan onTime = newLoan(student, LocalDateTime.now().plusDays(1), "in_progress");
        Loan returned = newLoan(student, LocalDateTime.now().minusDays(3), "returned");

        int updated = loanService.markOverdueLoans();

        assertThat(updated).isGreaterThanOrEqualTo(1);
        assertThat(loanRepository.findById(overdue.getId()).orElseThrow().getLoanStatus()).isEqualTo("late");
        assertThat(loanRepository.findById(onTime.getId()).orElseThrow().getLoanStatus())
                .isEqualTo("in_progress");
        assertThat(loanRepository.findById(returned.getId()).orElseThrow().getLoanStatus())
                .isEqualTo("returned");
    }
}
