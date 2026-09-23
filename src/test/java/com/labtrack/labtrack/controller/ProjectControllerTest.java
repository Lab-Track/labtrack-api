package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ProjectResponseDTO;
import com.labtrack.labtrack.service.ProjectService;
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
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Test
    void shouldReturn200WithProjectList() {
        ProjectResponseDTO project = ProjectResponseDTO.builder()
                .id(5L)
                .name("Sensores IoT")
                .professorName("Carlos Lima")
                .build();
        when(projectService.findAll()).thenReturn(List.of(project));

        ResponseEntity<List<ProjectResponseDTO>> response = projectController.getProjects();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Sensores IoT");
    }

    @Test
    void shouldReturn200WithEmptyList_WhenNoProjectsExist() {
        when(projectService.findAll()).thenReturn(List.of());

        ResponseEntity<List<ProjectResponseDTO>> response = projectController.getProjects();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
