package com.labtrack.labtrack.repository;

import com.labtrack.labtrack.model.Loan;
import com.labtrack.labtrack.model.Student;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Trava a linha do empréstimo para serializar devoluções concorrentes (duplo clique em "Devolver").
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l WHERE l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT l FROM Loan l WHERE l.student = :student AND l.loanStatus != 'returned'")
    List<Loan> findActiveLoansByStudent(@Param("student") Student student);

    @Query("SELECT l FROM Loan l WHERE l.loanStatus <> 'returned' ORDER BY l.checkoutDate DESC")
    List<Loan> findAllActiveOrderByCheckoutDateDesc();

    // RN05: atrasado = status 'late' já gravado OU prazo (prorrogado, se houver) vencido e ainda aberto.
    // A segunda condição torna a regra independente de o job de atraso já ter rodado.
    @Query("SELECT COUNT(l) > 0 FROM Loan l WHERE l.student = :student AND (l.loanStatus = 'late' OR " +
            "(l.loanStatus IN ('in_progress', 'partial') " +
            "AND COALESCE(l.extendedDate, l.expectedReturnDate) < :now))")
    boolean hasOverdueLoan(@Param("student") Student student, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Loan l SET l.loanStatus = 'late' WHERE l.loanStatus IN ('in_progress', 'partial') " +
            "AND COALESCE(l.extendedDate, l.expectedReturnDate) < :now")
    int markOverdueLoans(@Param("now") LocalDateTime now);
}
