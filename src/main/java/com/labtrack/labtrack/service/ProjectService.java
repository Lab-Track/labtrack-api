package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.ProjectResponseDTO;
import com.labtrack.labtrack.model.Project;
import com.labtrack.labtrack.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> findAll() {
        log.info("Buscando todos os projetos");
        return projectRepository.findAllWithProfessor().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private ProjectResponseDTO mapToResponseDTO(Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .professorName(project.getProfessor().getName())
                .build();
    }
}
