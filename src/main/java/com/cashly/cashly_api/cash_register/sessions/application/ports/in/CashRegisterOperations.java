package com.cashly.cashly_api.cash_register.sessions.application.ports.in;
import java.math.BigDecimal;
import java.util.UUID;
public interface CashRegisterOperations {
    void requireOpenRegister();
    void record(Entry entry);
    enum Direction { IN, OUT }
    enum EntryType { PAYMENT, INVOICE }
    record Entry(Direction direction, EntryType type, BigDecimal amount, UUID paymentMethodId,
            String description, String sourceType, UUID sourceId, UUID actorId, String actorName) {}
}
