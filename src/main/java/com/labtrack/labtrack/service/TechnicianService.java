package com.labtrack.labtrack.service;

import com.labtrack.labtrack.dto.TechnicianCreateRequest;
import com.labtrack.labtrack.dto.TechnicianResponse;
import com.labtrack.labtrack.exception.DuplicateTechnicianEmailException;
import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TechnicianResponse createTechnician(TechnicianCreateRequest request) {
        log.info("Cadastrando técnico com e-mail: {}", request.email());

        technicianRepository.findByLogin(request.email())
                .ifPresent(existing -> {
                    throw new DuplicateTechnicianEmailException(request.email());
                });

        Technician technician = new Technician();
        technician.setName(request.name());
        technician.setLogin(request.email());
        technician.setEmail(request.email());
        technician.setPasswordHash(passwordEncoder.encode(request.password()));

        Technician saved = technicianRepository.save(technician);

        log.info("Técnico cadastrado com id: {}", saved.getId());

        return new TechnicianResponse(saved.getId(), saved.getName(), saved.getEmail());
    }
}
