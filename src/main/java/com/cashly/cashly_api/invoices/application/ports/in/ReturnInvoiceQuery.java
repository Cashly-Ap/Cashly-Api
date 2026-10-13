package com.cashly.cashly_api.invoices.application.ports.in;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
public interface ReturnInvoiceQuery {
    Optional<Projection> findById(UUID invoiceId);
    record Projection(String number, String customerName, String customerRif, boolean cancelled, BigDecimal baseTotal, boolean hasSubtotal) {}
}
