package com.cashly.cashly_api.partners.customers.balance.application.ports.in;
import java.math.BigDecimal;
import java.util.UUID;
public interface ReturnBalanceMutation {
    void credit(Command command);
    void debit(Command command);
    record Command(UUID customerId, BigDecimal amount, UUID returnId,
            String returnNumber, String invoiceNumber, UUID actorId) {
    }
}
