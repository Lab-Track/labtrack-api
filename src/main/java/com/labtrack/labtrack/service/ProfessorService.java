package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.ProfessorResponseDTO;
import com.labtrack.labtrack.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    @Transactional(readOnly = true)
    public List<ProfessorResponseDTO> findAll() {
        log.info("Buscando todos os professores");
        return professorRepository.findAll(Sort.by("name")).stream()
                .map(professor -> ProfessorResponseDTO.builder()
                        .id(professor.getId())
                        .name(professor.getName())
                        .build())
                .toList();
    }
}
