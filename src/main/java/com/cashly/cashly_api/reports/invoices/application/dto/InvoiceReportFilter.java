package com.cashly.cashly_api.reports.invoices.application.dto;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public record InvoiceReportFilter(LocalDate dateFrom, LocalDate dateTo, UUID customerId, List<String> statuses, UUID productId, UUID categoryId) {
    public InvoiceReportFilter {
        statuses = statuses == null ? null : List.copyOf(statuses);
    }
}

