package com.cashly.cashly_api.cash_register.sessions.application.queries;

import com.cashly.cashly_api.cash_register.sessions.application.dto.CashRegisterMapper;
import com.cashly.cashly_api.cash_register.sessions.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cash_register.sessions.application.dto.DailyTotals;
import com.cashly.cashly_api.cash_register.sessions.domain.CashRegister;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.cash_register.sessions.domain.CashRegisterRepository;
import java.util.Optional;

public class GetActiveRegisterUseCase {

    private final CashRegisterRepository closureRepository;
    private final CashRegisterQueryGateway queries;
    public GetActiveRegisterUseCase(CashRegisterRepository closureRepository, CashRegisterQueryGateway queries) {
        this.closureRepository = closureRepository;
        this.queries = queries;
    }

    public Optional<CashRegisterResponse> execute() {
        return closureRepository.findActiveClosure().map(closure -> {
            var sessionTotals = queries.findSessionTotals(closure.getId());

            closure.applyTotals(
                    sessionTotals.totalPayments(), sessionTotals.paymentCount(),
                    sessionTotals.totalAdvances(), sessionTotals.advanceCount(),
                    sessionTotals.totalReturns(), sessionTotals.returnCount(),
                    sessionTotals.totalCreditNotes(), sessionTotals.totalDebitNotes(),
                    sessionTotals.totalInvoices(), sessionTotals.invoiceCount(),
                    sessionTotals.paymentDetails()
            );
            closure.applyMovementTotals(sessionTotals.totalRevenue(), sessionTotals.totalExpenses());

            var saved = closureRepository.save(closure);
            return CashRegisterMapper.toResponse(saved, sessionTotals,
                    closureRepository.findMovements(closure.getId()));
        });
    }
}
