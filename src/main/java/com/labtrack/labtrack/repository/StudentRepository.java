package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE s.registrationNumber = :registrationNumber")
    Optional<Student> findByRegistrationNumber(@Param("registrationNumber") String registrationNumber);
}