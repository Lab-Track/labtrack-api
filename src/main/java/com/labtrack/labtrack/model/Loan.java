package com.labtrack.labtrack.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_professor_id", nullable = false)
    private Professor responsibleProfessor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @OneToMany(mappedBy = "loan", fetch = FetchType.LAZY)
    private List<LoanItem> loanItems;

    @Column(name = "checkout_date", nullable = false)
    private LocalDateTime checkoutDate;

    @Column(name = "expected_return_date", nullable = false)
    private LocalDateTime expectedReturnDate;

    @Column(name = "extended_date")
    private LocalDateTime extendedDate;

    @Column(name = "loan_status", nullable = false)
    private String loanStatus;
}
