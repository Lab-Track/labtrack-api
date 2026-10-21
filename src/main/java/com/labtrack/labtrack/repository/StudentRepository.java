package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE s.registrationNumber = :registrationNumber")
    Optional<Student> findByRegistrationNumber(@Param("registrationNumber") String registrationNumber);

    // INSERT atômico: se duas requisições chegam com a mesma matrícula, a constraint UNIQUE faz uma delas
    // virar no-op (sem exceção), então a transação não é abortada e o chamador só relê o registro (RN06).
    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO student (registration_number, name, email, phone, reliability_rate, " +
            "registration_date) VALUES (:registrationNumber, :name, :email, :phone, 100, now()) " +
            "ON CONFLICT (registration_number) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("registrationNumber") String registrationNumber,
                       @Param("name") String name,
                       @Param("email") String email,
                       @Param("phone") String phone);
}
