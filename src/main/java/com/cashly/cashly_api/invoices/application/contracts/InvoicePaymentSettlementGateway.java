package com.cashly.cashly_api.invoices.application.contracts;
import java.math.BigDecimal;
import java.util.UUID;
public interface InvoicePaymentSettlementGateway {
    void settleCashInvoice(Settlement command);
    record Settlement(UUID customerId, UUID invoiceId, String invoiceNumber, BigDecimal amount,
            UUID paymentMethodId, UUID actorId, String actorName) {}
}
