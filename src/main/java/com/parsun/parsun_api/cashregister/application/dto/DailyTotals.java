package com.parsun.parsun_api.cashregister.application.dto;

import com.parsun.parsun_api.cashregister.domain.entities.CashRegisterDetail;

import java.math.BigDecimal;
import java.util.List;

public record DailyTotals(
        BigDecimal totalPayments,
        int paymentCount,
        BigDecimal totalAdvances,
        int advanceCount,
        BigDecimal totalReturns,
        int returnCount,
        BigDecimal totalCreditNotes,
        BigDecimal totalDebitNotes,
        BigDecimal totalInvoices,
        int invoiceCount,
        BigDecimal totalInvoiced,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        List<CashRegisterDetail> paymentDetails
) {
    public DailyTotals {
        if (totalInvoiced == null) totalInvoiced = totalInvoices;
        if (totalRevenue == null) totalRevenue = totalPayments;
        if (totalExpenses == null) totalExpenses = totalReturns.add(totalCreditNotes);
    }
}
