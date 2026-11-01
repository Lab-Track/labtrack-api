package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.ProfessorResponseDTO;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.repository.ProfessorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @InjectMocks
    private ProfessorService professorService;

    @Test
    void shouldReturnAllProfessorsOrderedByName() {
        Professor carlos = new Professor();
        carlos.setId(10L);
        carlos.setName("Carlos Lima");

        Professor ana = new Professor();
        ana.setId(5L);
        ana.setName("Ana Pereira");

        when(professorRepository.findAll(any(Sort.class))).thenReturn(List.of(ana, carlos));

        List<ProfessorResponseDTO> result = professorService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(5L);
        assertThat(result.get(0).getName()).isEqualTo("Ana Pereira");
        assertThat(result.get(1).getId()).isEqualTo(10L);
        assertThat(result.get(1).getName()).isEqualTo("Carlos Lima");

        verify(professorRepository).findAll(any(Sort.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoProfessorsRegistered() {
        when(professorRepository.findAll(any(Sort.class))).thenReturn(List.of());

        List<ProfessorResponseDTO> result = professorService.findAll();

        assertThat(result).isEmpty();
    }
}
