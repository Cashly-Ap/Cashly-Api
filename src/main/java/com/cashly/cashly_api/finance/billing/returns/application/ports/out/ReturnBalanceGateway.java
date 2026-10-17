package com.cashly.cashly_api.finance.billing.returns.application.ports.out;
import java.math.BigDecimal;
import java.util.UUID;
public interface ReturnBalanceGateway {
    void credit(Command command);
    void debit(Command command);
    record Command(UUID customerId, BigDecimal amount, UUID returnId, String returnNumber, String invoiceNumber, UUID actorId) {}
}
