package com.cashly.cashly_api.cash_register.reporting.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CashRegisterReportFilter(
        LocalDate dateFrom,
        LocalDate dateTo,
        UUID customerId,
        UUID productId,
        UUID categoryId
) {
    public CashRegisterReportFilter {
        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("Date range is required");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("Initial date cannot be after final date");
        }
    }

    public boolean hasCommercialFilters() {
        return customerId != null || productId != null || categoryId != null;
    }
}
