package com.labtrack.labtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labtrack.labtrack.dto.StudentCreateRequest;
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
class StudentControllerIT {

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
    void createStudentWithValidDataReturnsCreated() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest(
                "Carlos Pereira", "2023100", "carlos@email.com", "1177777777");

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Carlos Pereira"))
                .andExpect(jsonPath("$.registrationNumber").value("2023100"))
                .andExpect(jsonPath("$.email").value("carlos@email.com"))
                .andExpect(jsonPath("$.reliabilityRate").value(100));
    }

    @Test
    void createStudentWithoutTokenReturnsUnauthorized() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest(
                "Carlos Pereira", "2023101", "carlos@email.com", "1177777777");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createStudentWithDuplicateRegistrationNumberReturnsConflict() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest(
                "Carlos Pereira", "2023102", "carlos@email.com", "1177777777");
        String token = validToken();

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createStudentWithoutNameReturnsBadRequest() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest(
                null, "2023103", "carlos@email.com", "1177777777");

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createStudentWithoutRegistrationNumberReturnsBadRequest() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest(
                "Carlos Pereira", null, "carlos@email.com", "1177777777");

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createStudentWithoutEmailReturnsBadRequest() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest(
                "Carlos Pereira", "2023104", null, "1177777777");

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
