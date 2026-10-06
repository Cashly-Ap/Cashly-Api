package com.cashly.cashly_api.payments.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record InvoiceRemainingBalance(UUID invoiceId, BigDecimal remainingAmount) {

    public InvoiceRemainingBalance {
        Objects.requireNonNull(invoiceId, "Invoice id cannot be null");
        Objects.requireNonNull(remainingAmount, "Remaining amount cannot be null");
    }
}
