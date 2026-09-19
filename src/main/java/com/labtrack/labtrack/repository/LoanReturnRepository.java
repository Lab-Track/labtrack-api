package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.LoanReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanReturnRepository extends JpaRepository<LoanReturn, Long> {

    @Query("SELECT lr FROM LoanReturn lr " +
            "JOIN FETCH lr.loanItem li " +
            "JOIN FETCH li.loan l " +
            "JOIN FETCH l.student " +
            "JOIN FETCH l.responsibleProfessor " +
            "WHERE li.equipment.id = :equipmentId")
    List<LoanReturn> findByEquipmentId(@Param("equipmentId") Long equipmentId);
}