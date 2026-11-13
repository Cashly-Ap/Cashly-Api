package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;
import java.math.BigDecimal;
import java.util.UUID;
public interface CashRegisterGateway {
    void requireOpenRegister();
    boolean hasOpenRegister();
    void recordInvoice(InvoiceEntry entry);
    void cancelInvoice(UUID invoiceId);
    record InvoiceEntry(BigDecimal amount, UUID invoiceId, String invoiceNumber,
            UUID actorId, String actorName) {
    }
}
