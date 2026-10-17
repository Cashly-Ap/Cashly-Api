package com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record InvoiceLineId(UUID value) {

    public InvoiceLineId {
        Objects.requireNonNull(value, "InvoiceLineId cannot be null");
    }

    public static InvoiceLineId of(UUID value) {
        return new InvoiceLineId(value);
    }

    public static InvoiceLineId generate() {
        return new InvoiceLineId(UUID.randomUUID());
    }
}
