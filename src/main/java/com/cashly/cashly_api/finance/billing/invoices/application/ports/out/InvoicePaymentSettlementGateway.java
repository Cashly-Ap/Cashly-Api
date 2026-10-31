package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;
import java.math.BigDecimal;
import java.util.UUID;
public interface InvoicePaymentSettlementGateway {
    void settleCashInvoice(Settlement command);
    record Settlement(UUID customerId, UUID invoiceId, String invoiceNumber,
            BigDecimal amount, UUID paymentMethodId, UUID actorId,
            String actorName) {
    }
}
