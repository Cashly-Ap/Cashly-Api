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

        BigDecimal totalPayments = closure.getTotalPayments();
        BigDecimal totalAdvances = closure.getTotalAdvances();
        BigDecimal totalReturns = closure.getTotalReturns();
        BigDecimal totalCreditNotes = closure.getTotalCreditNotes();
        BigDecimal totalDebitNotes = closure.getTotalDebitNotes();
        BigDecimal totalInvoices = closure.getTotalInvoices();
        int paymentCount = closure.getPaymentCount();
        int advanceCount = closure.getAdvanceCount();
        int returnCount = closure.getReturnCount();
        int invoiceCount = closure.getInvoiceCount();
        BigDecimal totalInvoiced = closure.getTotalInvoices();
        BigDecimal totalRevenue = closure.getTotalPayments();
        BigDecimal totalExpenses = closure.getTotalReturns().add(closure.getTotalCreditNotes());
        BigDecimal balance = totalRevenue.subtract(totalExpenses);
        List<RevenueSummaryItem> revenueSummary = List.of();
        List<ExpenseSummaryItem> expenseSummary = List.of();

        if (dailyTotals != null) {
            totalPayments = dailyTotals.totalPayments();
            totalAdvances = dailyTotals.totalAdvances();
            totalReturns = dailyTotals.totalReturns();
            totalCreditNotes = dailyTotals.totalCreditNotes();
            totalDebitNotes = dailyTotals.totalDebitNotes();
            totalInvoices = dailyTotals.totalInvoices();
            paymentCount = dailyTotals.paymentCount();
            advanceCount = dailyTotals.advanceCount();
            returnCount = dailyTotals.returnCount();
            invoiceCount = dailyTotals.invoiceCount();

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
                closure.getVerifiedBy(),
                closure.getVerifiedByName(),
                closure.getVerifiedAt(),
                totalPayments, totalAdvances,
                totalReturns, totalCreditNotes, totalDebitNotes,
                totalInvoices,
                paymentCount, advanceCount, returnCount, invoiceCount,
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
