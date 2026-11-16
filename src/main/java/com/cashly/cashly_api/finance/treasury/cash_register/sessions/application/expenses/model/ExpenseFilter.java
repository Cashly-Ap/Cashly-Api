package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model;

import java.time.LocalDate;
import java.util.UUID;

public record ExpenseFilter(int page, int size, String sort,
        LocalDate dateFrom, LocalDate dateTo, UUID registerId,
        UUID paymentMethodId, String search) {

    public ExpenseFilter {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater");
        }
        if (size < 1 || size > 1000) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 1000");
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException(
                    "Initial date cannot be after final date");
        }
        if (sort == null || sort.isBlank()) {
            sort = "occurredAt,desc";
        }
    }
}
