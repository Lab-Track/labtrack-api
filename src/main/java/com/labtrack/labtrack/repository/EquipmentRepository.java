package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByCode(String code);

    @Query(
            value = "SELECT e FROM Equipment e WHERE " +
                    "(:status IS NULL OR e.currentStatus = :status) AND " +
                    "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR LOWER(e.code) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(e) FROM Equipment e WHERE " +
                    "(:status IS NULL OR e.currentStatus = :status) AND " +
                    "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR LOWER(e.code) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<Equipment> search(
            @Param("status") EquipmentStatus status,
            @Param("search") String search,
            Pageable pageable);
}
