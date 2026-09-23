package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Professor;
import com.labtrack.labtrack.model.Project;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProjectRepositoryIT {

    @Autowired
    private ProjectRepository projectRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findsAllProjectsWithProfessorAlreadyLoaded() {
        Professor professor = new Professor();
        professor.setName("Carlos Lima");
        professor.setEmail("carlos.lima." + System.nanoTime() + "@labtrack.local");
        entityManager.persist(professor);

        Project project = new Project();
        project.setName("Sensores IoT");
        project.setProfessor(professor);
        entityManager.persist(project);

        entityManager.flush();
        entityManager.clear();

        List<Project> projects = projectRepository.findAllWithProfessor();

        Project found = projects.stream()
                .filter(p -> p.getName().equals("Sensores IoT"))
                .findFirst()
                .orElseThrow();
        assertThat(found.getProfessor().getName()).isEqualTo("Carlos Lima");
    }
}
