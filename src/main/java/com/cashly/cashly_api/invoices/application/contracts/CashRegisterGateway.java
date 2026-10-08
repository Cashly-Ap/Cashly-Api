package com.cashly.cashly_api.invoices.application.contracts;
import java.math.BigDecimal;
import java.util.UUID;
public interface CashRegisterGateway {
    void requireOpenRegister();
    void recordInvoice(InvoiceEntry entry);
    record InvoiceEntry(BigDecimal amount, UUID invoiceId, String invoiceNumber, UUID actorId, String actorName) {}
}
