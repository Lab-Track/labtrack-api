package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.TechnicianCreateRequest;
import com.labtrack.labtrack.dto.TechnicianResponse;
import com.labtrack.labtrack.exception.DuplicateTechnicianEmailException;
import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.TechnicianRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private TechnicianService technicianService;

    @Test
    void shouldCreateTechnician_WhenEmailIsNew() {
        // Arrange
        technicianService = new TechnicianService(technicianRepository, passwordEncoder);
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                "Ana Lima", "ana@lab.com", "senha-qualquer");

        when(technicianRepository.findByLogin("ana@lab.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha-qualquer"))
                .thenReturn("hashed-senha-qualquer");
        when(technicianRepository.save(any(Technician.class)))
                .thenAnswer(invocation -> {
                    Technician saved = invocation.getArgument(0);
                    saved.setId(5L);
                    return saved;
                });

        // Act
        TechnicianResponse result = technicianService.createTechnician(request);

        // Assert
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Ana Lima");
        assertThat(result.email()).isEqualTo("ana@lab.com");

        ArgumentCaptor<Technician> captor = ArgumentCaptor.forClass(Technician.class);
        verify(technicianRepository).save(captor.capture());
        Technician saved = captor.getValue();
        assertThat(saved.getLogin()).isEqualTo("ana@lab.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-senha-qualquer");
    }

    @Test
    void shouldThrowDuplicateTechnicianEmailException_WhenEmailAlreadyExists() {
        // Arrange
        technicianService = new TechnicianService(technicianRepository, passwordEncoder);
        TechnicianCreateRequest request = new TechnicianCreateRequest(
                "Ana Lima", "ana@lab.com", "senha-qualquer");
        Technician existing = new Technician();
        existing.setLogin("ana@lab.com");

        when(technicianRepository.findByLogin("ana@lab.com"))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> technicianService.createTechnician(request))
                .isInstanceOf(DuplicateTechnicianEmailException.class)
                .hasMessageContaining("ana@lab.com");

        verify(technicianRepository, never()).save(any());
    }
}
