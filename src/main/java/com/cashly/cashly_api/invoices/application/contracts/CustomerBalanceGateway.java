package com.cashly.cashly_api.invoices.application.contracts;
import java.math.BigDecimal;
import java.util.UUID;
public interface CustomerBalanceGateway {
    void consumeInvoiceBalance(Consumption command);
    record Consumption(UUID customerId, BigDecimal amount, UUID invoiceId, String invoiceNumber, UUID actorId) {}
}
