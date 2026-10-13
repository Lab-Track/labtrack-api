package com.labtrack.labtrack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job que persiste o status 'late' nos empréstimos com prazo vencido, para que listagens e relatórios
 * enxerguem o atraso sem recalcular a data.
 */
@Component
@RequiredArgsConstructor
public class LoanOverdueScheduler {

    private final LoanService loanService;

    @Scheduled(
            initialDelayString = "${app.loan.overdue-check-interval-ms:900000}",
            fixedDelayString = "${app.loan.overdue-check-interval-ms:900000}")
    public void markOverdueLoans() {
        loanService.markOverdueLoans();
    }
}
