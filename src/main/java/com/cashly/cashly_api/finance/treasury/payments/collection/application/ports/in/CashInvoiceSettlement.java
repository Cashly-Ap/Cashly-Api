package com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in;
import java.math.BigDecimal;
import java.util.UUID;
public interface CashInvoiceSettlement {
    void settle(Command command);
    record Command(UUID customerId, UUID invoiceId, String invoiceNumber,
            BigDecimal amount, UUID paymentMethodId, UUID actorId,
            String actorName) {
    }
}
