package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterMapper;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;

import java.util.UUID;

public class CloseRegisterUseCase {

    private final CashRegisterRepository closureRepository;

    public CloseRegisterUseCase(CashRegisterRepository closureRepository) {
        this.closureRepository = closureRepository;
    }

    public CashRegisterResponse execute(UUID closureId, UUID closedBy, String closedByName, String closingNotes) {
        CashRegister closure = closureRepository.findById(RegisterId.of(closureId))
                .orElseThrow(() -> new IllegalArgumentException("Closure not found: " + closureId));

        var dailyTotals = closureRepository.findDailyTotals(closure.getClosureDate());

        closure.applyTotals(
                dailyTotals.totalPayments(), dailyTotals.paymentCount(),
                dailyTotals.totalAdvances(), dailyTotals.advanceCount(),
                dailyTotals.totalReturns(), dailyTotals.returnCount(),
                dailyTotals.totalCreditNotes(),
                dailyTotals.totalDebitNotes(),
                dailyTotals.totalInvoices(), dailyTotals.invoiceCount(),
                dailyTotals.paymentDetails()
        );

        closure.close(closedBy, closedByName, closingNotes);
        CashRegister saved = closureRepository.save(closure);
        return CashRegisterMapper.toResponse(saved, dailyTotals);
    }
}
