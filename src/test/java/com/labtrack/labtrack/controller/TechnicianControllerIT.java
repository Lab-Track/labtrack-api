package com.labtrack.labtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labtrack.labtrack.dto.LoginRequest;
import com.labtrack.labtrack.dto.TechnicianCreateRequest;
import com.labtrack.labtrack.repository.TechnicianRepository;
import com.labtrack.labtrack.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TechnicianControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TechnicianRepository technicianRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String validToken() {
        var technician = technicianRepository.findByLogin("tecnico.teste").orElseThrow();
        UserDetails userDetails = User.withUsername(technician.getLogin())
                .password(technician.getPasswordHash())
                .authorities("ROLE_TECHNICIAN")
                .build();
        return jwtService.generateToken(userDetails);
    }

    @Test
    void createTechnicianWithValidDataReturnsCreated() throws Exception {
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                "Ana Lima", "ana.lima@lab.com", "senha-qualquer");

        mockMvc.perform(post("/api/technician")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Ana Lima"))
                .andExpect(jsonPath("$.email").value("ana.lima@lab.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void createdTechnicianCanLogInImmediately() throws Exception {
        TechnicianCreateRequest createRequest = new TechnicianCreateRequest(
                "Bruno Melo", "bruno.melo@lab.com", "senha-do-bruno");

        mockMvc.perform(post("/api/technician")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("bruno.melo@lab.com", "senha-do-bruno");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void createTechnicianWithoutTokenReturnsUnauthorized() throws Exception {
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                "Ana Lima", "ana.unauth@lab.com", "senha-qualquer");

        mockMvc.perform(post("/api/technician")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTechnicianWithDuplicateEmailReturnsConflict() throws Exception {
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                "Ana Lima", "ana.duplicada@lab.com", "senha-qualquer");
        String token = validToken();

        mockMvc.perform(post("/api/technician")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/technician")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createTechnicianWithoutNameReturnsBadRequest() throws Exception {
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                null, "sem.nome@lab.com", "senha-qualquer");

        mockMvc.perform(post("/api/technician")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTechnicianWithoutEmailReturnsBadRequest() throws Exception {
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                "Sem Email", null, "senha-qualquer");

        mockMvc.perform(post("/api/technician")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTechnicianWithoutPasswordReturnsBadRequest() throws Exception {
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                "Sem Senha", "sem.senha@lab.com", null);

        mockMvc.perform(post("/api/technician")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
