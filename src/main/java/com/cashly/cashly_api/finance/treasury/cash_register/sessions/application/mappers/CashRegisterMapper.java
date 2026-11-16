package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.mappers;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterDetailResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.ExpenseSummaryItem;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.RevenueSummaryItem;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegisterDetail;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CashRegisterMapper {

    private CashRegisterMapper() {
    }

    public static CashRegisterResponse toResponse(CashRegister closure) {
        return toResponse(closure, null);
    }

    public static CashRegisterResponse toResponse(CashRegister closure,
            DailyTotals dailyTotals) {
        return toResponse(closure, dailyTotals, List.of());
    }

    public static CashRegisterResponse toResponse(CashRegister closure,
            DailyTotals dailyTotals, List<CashMovement> movements) {
        List<CashRegisterDetailResponse> details = closure.getDetails().stream()
                .map(CashRegisterMapper::toDetailResponse)
                .collect(Collectors.toList());

        BigDecimal totalInvoiced = closure.getTotalInvoices();
        BigDecimal totalRevenue = closure.getTotalPayments();
        BigDecimal totalExpenses = closure.getTotalReturns()
                .add(closure.getTotalCreditNotes());
        BigDecimal balance = totalRevenue.subtract(totalExpenses);
        List<RevenueSummaryItem> revenueSummary = List.of();
        List<ExpenseSummaryItem> expenseSummary = List.of();

        if (dailyTotals != null) {
            totalInvoiced = dailyTotals.totalInvoiced();
            totalRevenue = dailyTotals.totalRevenue();
            totalExpenses = dailyTotals.totalExpenses();
            balance = totalRevenue.subtract(totalExpenses);

            revenueSummary = buildRevenueSummary(dailyTotals);
            expenseSummary = buildExpenseSummary(movements);
        }

        return new CashRegisterResponse(closure.getId().value(),
                closure.getClosureDate(), closure.getStatus(),
                closure.getOpenedBy(), closure.getOpenedByName(),
                closure.getOpenedAt(), closure.getClosedBy(),
                closure.getClosedByName(), closure.getClosedAt(),
                closure.getClosingNotes(), details, totalInvoiced, totalRevenue,
                totalExpenses, balance, revenueSummary, expenseSummary,
                closure.getOpeningBalance(), closure.getTotalIncome(),
                closure.getTotalExpense(), closure.getExpectedClosingBalance(),
                closure.getActualClosingBalance(), closure.getDifference(),
                movements.stream().map(CashRegisterMapper::toMovementResponse)
                        .toList());
    }

    private static CashMovementResponse toMovementResponse(
            CashMovement movement) {
        return CashMovementResponse.from(movement);
    }

    private static List<RevenueSummaryItem> buildRevenueSummary(
            DailyTotals dailyTotals) {
        if (dailyTotals.paymentDetails() == null)
            return List.of();
        return dailyTotals.paymentDetails().stream()
                .map(d -> new RevenueSummaryItem(d.getPaymentMethodName(),
                        d.getExpectedAmount()))
                .collect(Collectors.toList());
    }

    private static List<ExpenseSummaryItem> buildExpenseSummary(
            List<CashMovement> movements) {
        Map<String, BigDecimal> totalsByPaymentMethod = new LinkedHashMap<>();
        for (CashMovement movement : movements) {
            if (movement.getDirection() != CashMovementDirection.OUT
                    || !movement.getType().affectsCashBalance()) {
                continue;
            }
            String concept = movement.getPaymentMethodName();
            if (concept == null || concept.isBlank()) {
                concept = "";
            }
            totalsByPaymentMethod.merge(concept, movement.getAmount(),
                    BigDecimal::add);
        }
        return totalsByPaymentMethod.entrySet().stream()
                .map(entry -> new ExpenseSummaryItem(entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    private static CashRegisterDetailResponse toDetailResponse(
            CashRegisterDetail detail) {
        return new CashRegisterDetailResponse(detail.getId(),
                detail.getPaymentMethodId(), detail.getPaymentMethodName(),
                detail.getTransactionCount(), detail.getExpectedAmount());
    }
}
