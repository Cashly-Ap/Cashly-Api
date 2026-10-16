package com.cashly.cashly_api.cash_register.sessions.application.dto;

import com.cashly.cashly_api.cash_register.sessions.application.movements.CashMovementResponse;

import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.RegisterStatus;

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
        String closingNotes,
        List<CashRegisterDetailResponse> details,
        BigDecimal totalInvoiced,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal balance,
        List<RevenueSummaryItem> revenueSummary,
        List<ExpenseSummaryItem> expenseSummary,
        BigDecimal openingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal expectedBalance,
        BigDecimal declaredBalance,
        BigDecimal difference,
        List<CashMovementResponse> movements
) {}
