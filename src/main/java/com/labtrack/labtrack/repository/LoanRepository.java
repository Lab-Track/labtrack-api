package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.student = :student AND l.loanStatus = 'ACTIVE'")
    List<Loan> findActiveLoansByStudent(@Param("student") Student student);

    @Query("SELECT l FROM Loan l WHERE l.student.registrationNumber = :registration AND l.loanStatus = 'ACTIVE'")
    List<Loan> findActiveLoansByStudentRegistration(@Param("registration") String registration);
}