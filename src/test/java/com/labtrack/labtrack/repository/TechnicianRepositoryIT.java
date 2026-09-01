package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Technician;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TechnicianRepositoryIT {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void findsSeededTestTechnicianByLoginWithMatchingPassword() {
        Optional<Technician> technician = technicianRepository.findByLogin("tecnico.teste");

        assertTrue(technician.isPresent());
        assertTrue(passwordEncoder.matches("Senha@123", technician.get().getPasswordHash()));
    }
}
