package com.parsun.parsun_api.cashregister.application.dto;

import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegisterDetail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class CashRegisterMapper {

    private CashRegisterMapper() {}

    public static CashRegisterResponse toResponse(CashRegister closure) {
        return toResponse(closure, null);
    }

    public static CashRegisterResponse toResponse(CashRegister closure, DailyTotals dailyTotals) {
        List<CashRegisterDetailResponse> details = closure.getDetails().stream()
                .map(CashRegisterMapper::toDetailResponse)
                .collect(Collectors.toList());

        BigDecimal totalInvoiced = closure.getTotalInvoices();
        BigDecimal totalRevenue = closure.getTotalPayments();
        BigDecimal totalExpenses = closure.getTotalReturns().add(closure.getTotalCreditNotes());
        BigDecimal balance = totalRevenue.subtract(totalExpenses);
        List<RevenueSummaryItem> revenueSummary = List.of();
        List<ExpenseSummaryItem> expenseSummary = List.of();

        if (dailyTotals != null) {
            totalInvoiced = dailyTotals.totalInvoiced();
            totalRevenue = dailyTotals.totalRevenue();
            totalExpenses = dailyTotals.totalExpenses();
            balance = totalRevenue.subtract(totalExpenses);

            revenueSummary = buildRevenueSummary(dailyTotals);
            expenseSummary = buildExpenseSummary(dailyTotals);
        }

        return new CashRegisterResponse(
                closure.getId().value(),
                closure.getClosureDate(),
                closure.getStatus(),
                closure.getOpenedBy(),
                closure.getOpenedByName(),
                closure.getOpenedAt(),
                closure.getClosedBy(),
                closure.getClosedByName(),
                closure.getClosedAt(),
                closure.getClosingNotes(),
                details,
                totalInvoiced, totalRevenue, totalExpenses, balance,
                revenueSummary, expenseSummary
        );
    }

    private static List<RevenueSummaryItem> buildRevenueSummary(DailyTotals dailyTotals) {
        if (dailyTotals.paymentDetails() == null) return List.of();
        return dailyTotals.paymentDetails().stream()
                .map(d -> new RevenueSummaryItem(
                        d.getPaymentMethodName(),
                        d.getExpectedAmount()))
                .collect(Collectors.toList());
    }

    private static List<ExpenseSummaryItem> buildExpenseSummary(DailyTotals dailyTotals) {
        List<ExpenseSummaryItem> items = new ArrayList<>();
        if (dailyTotals.totalReturns().compareTo(BigDecimal.ZERO) > 0) {
            items.add(new ExpenseSummaryItem("Devoluciones", dailyTotals.totalReturns()));
        }
        if (dailyTotals.totalCreditNotes().compareTo(BigDecimal.ZERO) > 0) {
            items.add(new ExpenseSummaryItem("Notas de Crédito", dailyTotals.totalCreditNotes()));
        }
        return items;
    }

    private static CashRegisterDetailResponse toDetailResponse(CashRegisterDetail detail) {
        return new CashRegisterDetailResponse(
                detail.getId(),
                detail.getPaymentMethodId(),
                detail.getPaymentMethodName(),
                detail.getTransactionCount(),
                detail.getExpectedAmount()
        );
    }
}
