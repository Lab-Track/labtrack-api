package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.LoanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanItemRepository extends JpaRepository<LoanItem, Long> {

    @Query("SELECT li FROM LoanItem li " +
            "JOIN FETCH li.loan l " +
            "JOIN FETCH l.student " +
            "JOIN FETCH l.responsibleProfessor " +
            "WHERE li.equipment.id = :equipmentId")
    List<LoanItem> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    long countByEquipmentIdAndItemStatus(Long equipmentId, String itemStatus);
}