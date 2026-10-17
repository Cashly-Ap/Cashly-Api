package com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record InvoiceId(UUID value) {

    public InvoiceId {
        Objects.requireNonNull(value, "InvoiceId cannot be null");
    }

    public static InvoiceId of(UUID value) {
        return new InvoiceId(value);
    }

    public static InvoiceId generate() {
        return new InvoiceId(UUID.randomUUID());
    }
}
