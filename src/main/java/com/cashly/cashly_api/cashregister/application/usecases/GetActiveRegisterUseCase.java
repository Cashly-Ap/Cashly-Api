package com.cashly.cashly_api.cashregister.application.usecases;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterMapper;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cashregister.application.dto.DailyTotals;
import com.cashly.cashly_api.cashregister.domain.entities.CashRegister;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import java.util.Optional;

public class GetActiveRegisterUseCase {

    private final CashRegisterRepository closureRepository;
    public GetActiveRegisterUseCase(CashRegisterRepository closureRepository) {
        this.closureRepository = closureRepository;
    }

    public Optional<CashRegisterResponse> execute() {
        return closureRepository.findActiveClosure().map(closure -> {
            var sessionTotals = closureRepository.findSessionTotals(closure.getId());

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
