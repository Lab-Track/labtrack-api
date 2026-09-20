package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    @Modifying
    @Query("DELETE FROM StatusHistory sh WHERE sh.equipment.id = :equipmentId")
    void deleteByEquipmentId(@Param("equipmentId") Long equipmentId);
}
