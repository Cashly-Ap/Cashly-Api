package com.parsun.parsun_api.cashregister.application.dto;

import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CashRegisterResponse(
        UUID id,
        LocalDate closureDate,
        RegisterStatus status,
        UUID openedBy,
        String openedByName,
        LocalDateTime openedAt,
        UUID closedBy,
        String closedByName,
        LocalDateTime closedAt,
        BigDecimal totalPayments,
        BigDecimal totalAdvances,
        BigDecimal totalReturns,
        BigDecimal totalCreditNotes,
        BigDecimal totalDebitNotes,
        BigDecimal totalInvoices,
        int paymentCount,
        int advanceCount,
        int returnCount,
        int invoiceCount,
        String closingNotes,
        List<CashRegisterDetailResponse> details,
        BigDecimal totalInvoiced,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal balance,
        List<RevenueSummaryItem> revenueSummary,
        List<ExpenseSummaryItem> expenseSummary
) {}
