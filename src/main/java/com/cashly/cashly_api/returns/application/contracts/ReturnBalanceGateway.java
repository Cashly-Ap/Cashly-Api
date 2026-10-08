package com.cashly.cashly_api.returns.application.contracts;
import java.math.BigDecimal;
import java.util.UUID;
public interface ReturnBalanceGateway {
    void credit(Command command);
    void debit(Command command);
    record Command(UUID customerId, BigDecimal amount, UUID returnId, String returnNumber, String invoiceNumber, UUID actorId) {}
}
