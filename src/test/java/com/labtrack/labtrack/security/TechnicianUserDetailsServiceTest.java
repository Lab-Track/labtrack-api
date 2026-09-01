package com.labtrack.labtrack.security;

import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.TechnicianRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicianUserDetailsServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Test
    void loadsUserDetailsForExistingTechnician() {
        Technician technician = new Technician();
        technician.setLogin("tecnico.teste");
        technician.setPasswordHash("hashed-password");
        when(technicianRepository.findByLogin("tecnico.teste")).thenReturn(Optional.of(technician));

        TechnicianUserDetailsService service = new TechnicianUserDetailsService(technicianRepository);
        UserDetails userDetails = service.loadUserByUsername("tecnico.teste");

        assertEquals("tecnico.teste", userDetails.getUsername());
        assertEquals("hashed-password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TECHNICIAN")));
    }

    @Test
    void throwsWhenTechnicianNotFound() {
        when(technicianRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        TechnicianUserDetailsService service = new TechnicianUserDetailsService(technicianRepository);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
    }
}
