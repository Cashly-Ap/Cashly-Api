package com.cashly.cashly_api.payments.application.api;
import java.math.BigDecimal;
import java.util.UUID;
public interface CashInvoiceSettlement {
    void settle(Command command);
    record Command(UUID customerId, UUID invoiceId, String invoiceNumber, BigDecimal amount,
            UUID paymentMethodId, UUID actorId, String actorName) {}
}
