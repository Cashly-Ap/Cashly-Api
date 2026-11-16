package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.close.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.mappers.CashRegisterMapper;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;

import java.util.UUID;
import java.math.BigDecimal;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class CloseRegisterUseCase {

    private final CashRegisterRepository closureRepository;
    private final CashRegisterQueryGateway queries;

    private final ActivityNotificationRecorder activityNotifications;

    public CloseRegisterUseCase(CashRegisterRepository closureRepository,
            CashRegisterQueryGateway queries,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.closureRepository = closureRepository;
        this.queries = queries;
    }

    @Audited(module = "finance", entityType = "CASH_REGISTER", action = "CLOSE")
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
        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_CASH_REGISTER",
                "CLOSE", "CASH_REGISTER", saved.getId().value(),
                saved.getClosureDate().toString(), closedBy));
        return CashRegisterMapper.toResponse(saved, dailyTotals,
                closureRepository.findMovements(closure.getId()));
    }
}
