package com.cashly.cashly_api.invoices.application.ports.in;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public record InvoiceDocumentFilter(LocalDate dateFrom, LocalDate dateTo, UUID customerId, List<String> statuses, UUID productId, UUID categoryId) {
    public InvoiceDocumentFilter {
        statuses = statuses == null ? null : List.copyOf(statuses);
    }
}

