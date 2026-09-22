package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.ProjectResponseDTO;
import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.Project;
import com.labtrack.labtrack.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldReturnAllProjectsMappedToResponseDTO() {
        Professor professor = new Professor();
        professor.setId(1L);
        professor.setName("Carlos Lima");

        Project project = new Project();
        project.setId(5L);
        project.setName("Sensores IoT");
        project.setProfessor(professor);

        when(projectRepository.findAllWithProfessor()).thenReturn(List.of(project));

        List<ProjectResponseDTO> result = projectService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
        assertThat(result.get(0).getName()).isEqualTo("Sensores IoT");
        assertThat(result.get(0).getProfessorName()).isEqualTo("Carlos Lima");
    }

    @Test
    void shouldReturnEmptyList_WhenNoProjectsExist() {
        when(projectRepository.findAllWithProfessor()).thenReturn(List.of());

        List<ProjectResponseDTO> result = projectService.findAll();

        assertThat(result).isEmpty();
    }
}
