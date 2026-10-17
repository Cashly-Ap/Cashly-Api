package com.cashly.cashly_api.finance.billing.returns.application.ports.out;
import java.math.BigDecimal;
import java.util.UUID;
public interface ReturnCashGateway {
    void record(Entry entry);
    record Entry(BigDecimal amount, UUID returnId, String returnNumber, UUID actorId, String actorName) {}
}
