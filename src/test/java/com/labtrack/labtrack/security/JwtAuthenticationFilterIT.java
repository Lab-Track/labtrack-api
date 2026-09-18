package com.labtrack.labtrack.security;

import com.labtrack.labtrack.repository.TechnicianRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Value("${jwt.secret}")
    private String configuredSecret;

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/some-protected-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithMalformedTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/some-protected-resource")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithExpiredTokenIsUnauthorized() throws Exception {
        JwtService expiredTokenService = new JwtService(configuredSecret, -1000L);
        UserDetails userDetails = User.withUsername("tecnico.teste")
                .password("irrelevant")
                .authorities("ROLE_TECHNICIAN")
                .build();
        String expiredToken = expiredTokenService.generateToken(userDetails);

        mockMvc.perform(get("/api/some-protected-resource")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithValidTokenPassesSecurityLayer() throws Exception {
        var technician = technicianRepository.findByLogin("tecnico.teste").orElseThrow();
        UserDetails userDetails = User.withUsername(technician.getLogin())
                .password(technician.getPasswordHash())
                .authorities("ROLE_TECHNICIAN")
                .build();
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(get("/api/students/2021001/active-loans")  // ← Valor fixo em vez de {registration}
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());  // Aluno não existe → 404 esperado
    }
}
