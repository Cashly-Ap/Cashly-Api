package com.cashly.cashly_api.partners.customers.balance.application.ports.in;
import java.math.BigDecimal;
import java.util.UUID;
public interface InvoiceBalanceConsumption {
    void consume(Command command);
    record Command(UUID customerId, BigDecimal amount, UUID invoiceId, String invoiceNumber, UUID actorId) {}
}
