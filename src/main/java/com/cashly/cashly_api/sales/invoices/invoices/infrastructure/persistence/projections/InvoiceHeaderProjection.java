package com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.projections;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface InvoiceHeaderProjection {
    UUID getId();

    UUID getCustomerId();

    String getInvoiceNumber();

    String getStatus();

    BigDecimal getSubtotal();

    BigDecimal getReturnedTotal();

    LocalDate getDueDate();
}
