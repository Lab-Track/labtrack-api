package com.labtrack.labtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.labtrack.labtrack.dto.LoginRequest;
import com.labtrack.labtrack.dto.LoginResponse;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.LoanItem;
import com.labtrack.labtrack.model.LoanReturn;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.Student;
import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.LoanReturnRepository;
import com.labtrack.labtrack.repository.StudentRepository;
import com.labtrack.labtrack.repository.TechnicianRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EquipmentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanItemRepository loanItemRepository;

    @Autowired
    private LoanReturnRepository loanReturnRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String jwtToken;

    @BeforeEach
    void authenticate() throws Exception {
        LoginRequest request = new LoginRequest("tecnico.teste", "Senha@123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        jwtToken = objectMapper.readValue(response, LoginResponse.class).token();
    }

    @Test
    void getLoanHistoryReturnsEventsOrderedByDateDescendingAndPaginated() throws Exception {
        Professor professor = new Professor();
        professor.setName("Carlos Lima");
        professor.setEmail("carlos.lima@labtrack.local");
        entityManager.persist(professor);

        Student studentA = new Student();
        studentA.setName("Ana Souza");
        studentA.setEmail("ana.souza@labtrack.local");
        studentA.setReliabilityRate(new BigDecimal("100.00"));
        studentA.setRegistrationDate(LocalDateTime.now());
        studentRepository.save(studentA);

        Student studentB = new Student();
        studentB.setName("Bruno Lima");
        studentB.setEmail("bruno.lima@labtrack.local");
        studentB.setReliabilityRate(new BigDecimal("100.00"));
        studentB.setRegistrationDate(LocalDateTime.now());
        studentRepository.save(studentB);

        Technician technician = technicianRepository.findByLogin("tecnico.teste").orElseThrow();

        Equipment equipment = new Equipment();
        equipment.setName("Osciloscópio");
        equipment.setIdentificationPhoto("osciloscopio.jpg");
        equipment.setCurrentStatus("available");
        equipment.setQuantity(1);
        equipmentRepository.save(equipment);

        Loan loanA = new Loan();
        loanA.setStudent(studentA);
        loanA.setResponsibleProfessor(professor);
        loanA.setTechnician(technician);
        loanA.setCheckoutDate(LocalDateTime.of(2026, 9, 1, 10, 0));
        loanA.setExpectedReturnDate(LocalDateTime.of(2026, 9, 8, 10, 0));
        loanA.setLoanStatus("returned");
        loanRepository.save(loanA);

        LoanItem loanItemA = new LoanItem();
        loanItemA.setLoan(loanA);
        loanItemA.setEquipment(equipment);
        loanItemA.setCheckoutPhoto("checkout_a.jpg");
        loanItemA.setCheckoutCondition("GOOD");
        loanItemA.setItemStatus("returned");
        loanItemRepository.save(loanItemA);

        LoanReturn loanReturnA = new LoanReturn();
        loanReturnA.setLoanItem(loanItemA);
        loanReturnA.setReturnDate(LocalDateTime.of(2026, 9, 2, 9, 0));
        loanReturnA.setReturnCondition("GOOD");
        loanReturnA.setVerificationStatus("working");
        loanReturnA.setOverdue(false);
        loanReturnRepository.save(loanReturnA);

        Loan loanB = new Loan();
        loanB.setStudent(studentB);
        loanB.setResponsibleProfessor(professor);
        loanB.setTechnician(technician);
        loanB.setCheckoutDate(LocalDateTime.of(2026, 9, 5, 14, 0));
        loanB.setExpectedReturnDate(LocalDateTime.of(2026, 9, 12, 14, 0));
        loanB.setLoanStatus("in_progress");
        loanRepository.save(loanB);

        LoanItem loanItemB = new LoanItem();
        loanItemB.setLoan(loanB);
        loanItemB.setEquipment(equipment);
        loanItemB.setCheckoutPhoto("checkout_b.jpg");
        loanItemB.setCheckoutCondition("GOOD");
        loanItemB.setItemStatus("loaned");
        loanItemRepository.save(loanItemB);

        mockMvc.perform(get("/api/equipments/{id}/history", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].eventType").value("RETIRADA"))
                .andExpect(jsonPath("$.content[0].studentName").value("Bruno Lima"))
                .andExpect(jsonPath("$.content[1].eventType").value("DEVOLUCAO"))
                .andExpect(jsonPath("$.content[1].studentName").value("Ana Souza"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getLoanHistoryReturns200WithEmptyContent_WhenEquipmentHasNoLoans() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setName("Multímetro sem uso");
        equipment.setIdentificationPhoto("multimetro.jpg");
        equipment.setCurrentStatus("available");
        equipment.setQuantity(1);
        equipmentRepository.save(equipment);

        mockMvc.perform(get("/api/equipments/{id}/history", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getLoanHistoryReturns404_WhenEquipmentDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/equipments/{id}/history", 999999L)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getLoanHistoryReturns401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/equipments/{id}/history", 1L))
                .andExpect(status().isUnauthorized());
    }
}