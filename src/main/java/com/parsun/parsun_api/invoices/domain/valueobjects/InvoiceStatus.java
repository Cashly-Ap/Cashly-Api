package com.parsun.parsun_api.invoices.domain.valueobjects;

public enum InvoiceStatus {
    DRAFT,
    PAID,
    PENDING_PAYMENT,
    PARTIALLY_PAID,
    FULLY_RETURNED,
    CANCELLED
}
