package com.labtrack.labtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.labtrack.labtrack.dto.LoginRequest;
import com.labtrack.labtrack.dto.LoginResponse;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.Project;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerIT {

    @Autowired
    private MockMvc mockMvc;

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
    void getProjectsReturns200WithProjectAndProfessorName() throws Exception {
        Professor professor = new Professor();
        professor.setName("Carlos Lima");
        professor.setEmail("carlos.lima." + System.nanoTime() + "@labtrack.local");
        entityManager.persist(professor);

        Project project = new Project();
        project.setName("Sensores IoT " + System.nanoTime());
        project.setProfessor(professor);
        entityManager.persist(project);
        entityManager.flush();

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + project.getId() + ")].professorResponsavel")
                        .value("Carlos Lima"));
    }

    @Test
    void getProjectsReturns401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }
}
