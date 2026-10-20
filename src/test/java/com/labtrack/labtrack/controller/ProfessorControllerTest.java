package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ProfessorResponseDTO;
import com.labtrack.labtrack.service.ProfessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessorControllerTest {

    @Mock
    private ProfessorService professorService;

    @InjectMocks
    private ProfessorController professorController;

    @Test
    void shouldReturn200WithProfessorList() {
        ProfessorResponseDTO professor = ProfessorResponseDTO.builder().id(10L).name("Carlos Lima").build();
        when(professorService.findAll()).thenReturn(List.of(professor));

        ResponseEntity<List<ProfessorResponseDTO>> response = professorController.getProfessors();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Carlos Lima");
    }

    @Test
    void shouldReturn200WithEmptyListWhenNoProfessors() {
        when(professorService.findAll()).thenReturn(List.of());

        ResponseEntity<List<ProfessorResponseDTO>> response = professorController.getProfessors();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
