package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Equipment;
import com.labtrack.labtrack.model.EquipmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @Query(
            value = "SELECT e FROM Equipment e WHERE " +
                    "(:status IS NULL OR e.currentStatus = :status) AND " +
                    "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
                    "OR LOWER(e.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
                    "(:projectId IS NULL OR e.project.id = :projectId) " +
                    // Desempate por id: linhas com o mesmo createdAt (ex.: seeds inseridos no
                    // mesmo INSERT, onde now() é avaliado uma única vez) deixam o ORDER BY só
                    // por createdAt instável entre execuções, fazendo a paginação por OFFSET
                    // repetir ou pular itens entre páginas.
                    "ORDER BY e.createdAt DESC, e.id DESC",
            countQuery = "SELECT COUNT(e) FROM Equipment e WHERE " +
                    "(:status IS NULL OR e.currentStatus = :status) AND " +
                    "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
                    "OR LOWER(e.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
                    "(:projectId IS NULL OR e.project.id = :projectId)"
    )
    Page<Equipment> search(
            @Param("status") EquipmentStatus status,
            @Param("search") String search,
            @Param("projectId") Long projectId,
            Pageable pageable);

    // Trava as linhas (ordenadas por id para evitar deadlock) enquanto o empréstimo é validado e gravado,
    // para que dois empréstimos simultâneos do mesmo equipamento não passem juntos pela checagem da RN01.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id IN :ids ORDER BY e.id")
    List<Equipment> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
