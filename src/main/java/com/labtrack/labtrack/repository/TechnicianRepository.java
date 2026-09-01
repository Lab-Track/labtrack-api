package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    Optional<Technician> findByLogin(String login);
}
