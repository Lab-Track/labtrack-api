package com.labtrack.labtrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_return")
@Getter
@Setter
@NoArgsConstructor
public class LoanReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_item_id", nullable = false, unique = true)
    private LoanItem loanItem;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "return_condition", nullable = false)
    private String returnCondition;

    @Column(name = "notes")
    private String notes;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus;

    @Column(name = "overdue", nullable = false)
    private boolean overdue;
}
