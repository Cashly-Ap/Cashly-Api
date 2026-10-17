package com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record InvoiceReturnAdjustment(UUID invoiceId, BigDecimal total, List<Line> lines, boolean closeInvoice) {
    public InvoiceReturnAdjustment {
        Objects.requireNonNull(invoiceId, "Invoice ID cannot be null");
        Objects.requireNonNull(total, "Return total cannot be null");
        if (total.signum() < 0) {
            throw new IllegalArgumentException("Return total cannot be negative");
        }
        lines = List.copyOf(lines);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Return must contain at least one line");
        }
    }

    public record Line(UUID invoiceLineId, BigDecimal quantity) {
        public Line {
            Objects.requireNonNull(invoiceLineId, "Invoice line ID cannot be null");
            Objects.requireNonNull(quantity, "Return quantity cannot be null");
            if (quantity.signum() <= 0) {
                throw new IllegalArgumentException("Return quantity must be positive");
            }
        }
    }
}
