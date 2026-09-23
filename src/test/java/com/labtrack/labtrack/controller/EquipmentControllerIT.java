package com.labtrack.labtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.labtrack.labtrack.dto.LoginRequest;
import com.labtrack.labtrack.dto.LoginResponse;
import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.LoanItem;
import com.labtrack.labtrack.model.LoanReturn;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.StatusHistory;
import com.labtrack.labtrack.model.Student;
import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.EquipmentRepository;
import com.labtrack.labtrack.repository.LoanItemRepository;
import com.labtrack.labtrack.repository.LoanRepository;
import com.labtrack.labtrack.repository.LoanReturnRepository;
import com.labtrack.labtrack.repository.StatusHistoryRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

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
        equipment.setCurrentStatus(EquipmentStatus.DISPONIVEL);
        equipment.setCode("EQP-" + System.nanoTime());
        equipment.setCategory("Medição");
        equipment.setLaboratory("Laboratório de Eletrônica");
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

        mockMvc.perform(get("/api/equipment/{id}/history", equipment.getId())
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
    void getLoanHistoryGroupsMultipleUnitsOfSameCheckout_WithQuantity() throws Exception {
        Professor professor = new Professor();
        professor.setName("Carlos Lima");
        professor.setEmail("professor." + System.nanoTime() + "@labtrack.local");
        entityManager.persist(professor);

        Student student = new Student();
        student.setName("Ana Souza");
        student.setEmail("aluno." + System.nanoTime() + "@labtrack.local");
        student.setReliabilityRate(new BigDecimal("100.00"));
        student.setRegistrationDate(LocalDateTime.now());
        studentRepository.save(student);

        Equipment equipment = new Equipment();
        equipment.setName("Multímetro em par");
        equipment.setIdentificationPhoto("multimetro.jpg");
        equipment.setCurrentStatus(EquipmentStatus.DISPONIVEL);
        equipment.setCode("EQP-" + System.nanoTime());
        equipment.setCategory("Medição");
        equipment.setLaboratory("Laboratório de Eletrônica");
        equipment.setQuantity(2);
        equipmentRepository.save(equipment);

        Loan loan = new Loan();
        loan.setStudent(student);
        loan.setResponsibleProfessor(professor);
        loan.setTechnician(technicianRepository.findByLogin("tecnico.teste").orElseThrow());
        loan.setCheckoutDate(LocalDateTime.of(2026, 9, 10, 9, 0));
        loan.setExpectedReturnDate(LocalDateTime.of(2026, 9, 17, 9, 0));
        loan.setLoanStatus("in_progress");
        loanRepository.save(loan);

        LoanItem itemOne = new LoanItem();
        itemOne.setLoan(loan);
        itemOne.setEquipment(equipment);
        itemOne.setCheckoutPhoto("checkout_1.jpg");
        itemOne.setCheckoutCondition("GOOD");
        itemOne.setItemStatus("loaned");
        loanItemRepository.save(itemOne);

        LoanItem itemTwo = new LoanItem();
        itemTwo.setLoan(loan);
        itemTwo.setEquipment(equipment);
        itemTwo.setCheckoutPhoto("checkout_2.jpg");
        itemTwo.setCheckoutCondition("GOOD");
        itemTwo.setItemStatus("loaned");
        loanItemRepository.save(itemTwo);

        mockMvc.perform(get("/api/equipment/{id}/history", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].eventType").value("RETIRADA"))
                .andExpect(jsonPath("$.content[0].quantity").value(2));
    }

    @Test
    void getLoanHistoryReturns200WithEmptyContent_WhenEquipmentHasNoLoans() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setName("Multímetro sem uso");
        equipment.setIdentificationPhoto("multimetro.jpg");
        equipment.setCurrentStatus(EquipmentStatus.DISPONIVEL);
        equipment.setCode("EQP-" + System.nanoTime());
        equipment.setCategory("Medição");
        equipment.setLaboratory("Laboratório de Eletrônica");
        equipment.setQuantity(1);
        equipmentRepository.save(equipment);

        mockMvc.perform(get("/api/equipment/{id}/history", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getLoanHistoryReturns404_WhenEquipmentDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}/history", 999999L)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getLoanHistoryReturns400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}/history", 1L)
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLoanHistoryReturns400_WhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}/history", 1L)
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLoanHistoryReturns400_WhenSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}/history", 1L)
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLoanHistoryReturns400_WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}/history", "abc")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLoanHistoryReturns401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}/history", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEquipmentReturnsFilteredCatalog_WhenStatusAndSearchProvided() throws Exception {
        Equipment matching = saveEquipment(EquipmentStatus.DISPONIVEL);
        matching.setName("Osciloscópio Digital " + System.nanoTime());
        equipmentRepository.save(matching);

        Equipment wrongStatus = saveEquipment(EquipmentStatus.MANUTENCAO);
        wrongStatus.setName(matching.getName());
        equipmentRepository.save(wrongStatus);

        mockMvc.perform(get("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("status", "DISPONIVEL")
                        .param("search", "osciloscópio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + matching.getId() + ")]").exists())
                .andExpect(jsonPath("$.content[?(@.id == " + wrongStatus.getId() + ")]").doesNotExist());
    }

    @Test
    void getEquipmentReturns200WithAllEquipment_WhenNoFiltersProvided() throws Exception {
        saveEquipment(EquipmentStatus.DISPONIVEL);

        mockMvc.perform(get("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getEquipmentReturns401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEquipmentByIdReturns200WithEquipment_WhenFound() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);

        mockMvc.perform(get("/api/equipment/{id}", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(equipment.getId()))
                .andExpect(jsonPath("$.nome").value("Equipamento de teste"));
    }

    @Test
    void getEquipmentByIdReturns404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}", 999999L)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getEquipmentByIdReturns401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/equipment/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEquipmentReturns201WithFrontendFieldNames() throws Exception {
        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEquipmentBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Multímetro Digital"))
                .andExpect(jsonPath("$.codigo").value(org.hamcrest.Matchers.matchesPattern("EQP-\\d{4,}")))
                .andExpect(jsonPath("$.fotoUrl").value("multimetro.jpg"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.categoria").value("Medição"))
                .andExpect(jsonPath("$.laboratorio").value("Laboratório de Eletrônica"))
                .andExpect(jsonPath("$.qtdTotal").value(3))
                .andExpect(jsonPath("$.qtdDisponivel").value(3))
                .andExpect(jsonPath("$.cadastradoEm").isNotEmpty())
                .andExpect(jsonPath("$.projeto").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.bancada").doesNotExist());
    }

    @Test
    void createEquipmentGeneratesDifferentCodes_ForEachEquipmentCreated() throws Exception {
        String body = objectMapper.writeValueAsString(newEquipmentBody());

        MvcResult first = mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String firstCode = objectMapper.readTree(first.getResponse().getContentAsString()).get("codigo").asText();
        String secondCode = objectMapper.readTree(second.getResponse().getContentAsString()).get("codigo").asText();

        assertThat(firstCode).isNotEqualTo(secondCode);
    }

    @Test
    void createEquipmentReturns201WithDefaultCategory_WhenCategoryOmitted() throws Exception {
        Map<String, Object> body = newEquipmentBody();
        body.remove("categoria");

        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("Sem categoria"));
    }

    @Test
    void createEquipmentReturns400_WhenStatusIsNotInEnum() throws Exception {
        Map<String, Object> body = newEquipmentBody();
        body.put("status", "available");

        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createEquipmentReturns400_WhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteEquipmentReturns204AndRemovesEquipmentAndItsStatusHistory() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);
        saveStatusHistory(equipment);
        // Contexto limpo, como numa requisição real: o service carrega o equipamento do zero.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/equipment/{id}", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(equipmentRepository.existsById(equipment.getId())).isFalse();
        Long remainingHistory = entityManager
                .createQuery("SELECT COUNT(sh) FROM StatusHistory sh WHERE sh.equipment.id = :id", Long.class)
                .setParameter("id", equipment.getId())
                .getSingleResult();
        assertThat(remainingHistory).isZero();
    }

    @Test
    void deleteEquipmentReturns409_WhenStatusIsEmprestado() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.EMPRESTADO);

        mockMvc.perform(delete("/api/equipment/{id}", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertThat(equipmentRepository.existsById(equipment.getId())).isTrue();
    }

    @Test
    void deleteEquipmentReturns409_WhenLoanItemIsLoanedEvenIfStatusIsDisponivel() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);
        saveLoanItem(equipment, "loaned", "in_progress");

        mockMvc.perform(delete("/api/equipment/{id}", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("empréstimo ativo")));

        assertThat(equipmentRepository.existsById(equipment.getId())).isTrue();
    }

    @Test
    void deleteEquipmentReturns409_WhenEquipmentHasLoanHistory() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);
        saveLoanItem(equipment, "returned", "returned");

        mockMvc.perform(delete("/api/equipment/{id}", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(containsString("histórico de empréstimos")));

        assertThat(equipmentRepository.existsById(equipment.getId())).isTrue();
    }

    @Test
    void deleteEquipmentReturns404_WhenEquipmentDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/equipment/{id}", 999999L)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteEquipmentReturns401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(delete("/api/equipment/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateStatusReturns200AndRecordsHistoryWithReasonAndTechnician() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);

        mockMvc.perform(patch("/api/equipment/{id}/status", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MANUTENCAO\",\"motivo\":\"Display com defeito\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(equipment.getId()))
                .andExpect(jsonPath("$.status").value("MANUTENCAO"))
                .andExpect(jsonPath("$.qtdDisponivel").value(1));

        StatusHistory history = entityManager
                .createQuery("SELECT sh FROM StatusHistory sh WHERE sh.equipment.id = :id", StatusHistory.class)
                .setParameter("id", equipment.getId())
                .getSingleResult();
        assertThat(history.getPreviousStatus()).isEqualTo("DISPONIVEL");
        assertThat(history.getNewStatus()).isEqualTo("MANUTENCAO");
        assertThat(history.getReason()).isEqualTo("Display com defeito");
        assertThat(history.getTechnician().getLogin()).isEqualTo("tecnico.teste");
    }

    @Test
    void updateStatusReturns400_WhenStatusIsNotInEnum() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);

        mockMvc.perform(patch("/api/equipment/{id}/status", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EXPLODIDO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateStatusReturns400_WhenStatusIsMissing() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);

        mockMvc.perform(patch("/api/equipment/{id}/status", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"sem status\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateStatusReturns404_WhenEquipmentDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/equipment/{id}/status", 999999L)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MANUTENCAO\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateStatusReturns401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(patch("/api/equipment/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MANUTENCAO\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateStatusReturns409_WhenNewStatusIsEmprestado() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);

        mockMvc.perform(patch("/api/equipment/{id}/status", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EMPRESTADO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("EMPRESTADO")));
    }

    @Test
    void updateStatusReturns409_WhenLoanItemIsLoanedEvenIfStatusIsDisponivel() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.DISPONIVEL);
        saveLoanItem(equipment, "loaned", "in_progress");

        mockMvc.perform(patch("/api/equipment/{id}/status", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MANUTENCAO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("empréstimo ativo")));
    }

    @Test
    void updateStatusIsIdempotent_WhenStatusIsUnchanged() throws Exception {
        Equipment equipment = saveEquipment(EquipmentStatus.MANUTENCAO);

        mockMvc.perform(patch("/api/equipment/{id}/status", equipment.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MANUTENCAO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MANUTENCAO"));

        Long historyCount = entityManager
                .createQuery("SELECT COUNT(sh) FROM StatusHistory sh WHERE sh.equipment.id = :id", Long.class)
                .setParameter("id", equipment.getId())
                .getSingleResult();
        assertThat(historyCount).isZero();
    }

    private Equipment saveEquipment(EquipmentStatus status) {
        Equipment equipment = new Equipment();
        equipment.setName("Equipamento de teste");
        equipment.setIdentificationPhoto("teste.jpg");
        equipment.setCurrentStatus(status);
        equipment.setCode("EQP-IT-" + System.nanoTime());
        equipment.setCategory("Medição");
        equipment.setLaboratory("Laboratório de Eletrônica");
        equipment.setQuantity(1);
        return equipmentRepository.save(equipment);
    }

    private void saveStatusHistory(Equipment equipment) {
        StatusHistory history = new StatusHistory();
        history.setEquipment(equipment);
        history.setPreviousStatus("DISPONIVEL");
        history.setNewStatus("MANUTENCAO");
        history.setChangeDate(LocalDateTime.now());
        history.setTechnician(technicianRepository.findByLogin("tecnico.teste").orElseThrow());
        statusHistoryRepository.save(history);
    }

    private void saveLoanItem(Equipment equipment, String itemStatus, String loanStatus) {
        Professor professor = new Professor();
        professor.setName("Carlos Lima");
        professor.setEmail("professor." + System.nanoTime() + "@labtrack.local");
        entityManager.persist(professor);

        Student student = new Student();
        student.setName("Ana Souza");
        student.setEmail("aluno." + System.nanoTime() + "@labtrack.local");
        student.setReliabilityRate(new BigDecimal("100.00"));
        student.setRegistrationDate(LocalDateTime.now());
        studentRepository.save(student);

        Loan loan = new Loan();
        loan.setStudent(student);
        loan.setResponsibleProfessor(professor);
        loan.setTechnician(technicianRepository.findByLogin("tecnico.teste").orElseThrow());
        loan.setCheckoutDate(LocalDateTime.of(2026, 9, 1, 10, 0));
        loan.setExpectedReturnDate(LocalDateTime.of(2026, 9, 8, 10, 0));
        loan.setLoanStatus(loanStatus);
        loanRepository.save(loan);

        LoanItem item = new LoanItem();
        item.setLoan(loan);
        item.setEquipment(equipment);
        item.setCheckoutPhoto("checkout.jpg");
        item.setCheckoutCondition("GOOD");
        item.setItemStatus(itemStatus);
        loanItemRepository.save(item);
    }

    private Map<String, Object> newEquipmentBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Multímetro Digital");
        body.put("fotoUrl", "multimetro.jpg");
        body.put("categoria", "Medição");
        body.put("laboratorio", "Laboratório de Eletrônica");
        body.put("qtdTotal", 3);
        return body;
    }
}