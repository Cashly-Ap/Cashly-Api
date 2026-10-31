package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.close.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.mappers.CashRegisterMapper;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;

import java.util.UUID;
import java.math.BigDecimal;

public class CloseRegisterUseCase {

    private final CashRegisterRepository closureRepository;
    private final CashRegisterQueryGateway queries;

    public CloseRegisterUseCase(CashRegisterRepository closureRepository,
            CashRegisterQueryGateway queries) {
        this.closureRepository = closureRepository;
        this.queries = queries;
    }

    public CashRegisterResponse execute(UUID closureId, UUID closedBy,
            String closedByName, BigDecimal declaredBalance,
            String closingNotes) {
        CashRegister closure = closureRepository
                .findById(RegisterId.of(closureId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Closure not found: " + closureId));

        var dailyTotals = queries.findSessionTotals(closure.getId());

        closure.applyTotals(dailyTotals.totalPayments(),
                dailyTotals.paymentCount(), dailyTotals.totalAdvances(),
                dailyTotals.advanceCount(), dailyTotals.totalReturns(),
                dailyTotals.returnCount(), dailyTotals.totalCreditNotes(),
                dailyTotals.totalDebitNotes(), dailyTotals.totalInvoices(),
                dailyTotals.invoiceCount(), dailyTotals.paymentDetails());
        closure.applyMovementTotals(dailyTotals.totalRevenue(),
                dailyTotals.totalExpenses());

        closure.close(closedBy, closedByName, declaredBalance, closingNotes);
        CashRegister saved = closureRepository.save(closure);
        return CashRegisterMapper.toResponse(saved, dailyTotals,
                closureRepository.findMovements(closure.getId()));
    }
}
