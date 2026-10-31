package com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out;
import java.math.BigDecimal;
import java.util.UUID;
public interface CashRegisterGateway {
    void requireOpenRegister();
    void recordMovement(Movement movement);
    void recordPayment(PaymentEntry entry);
    record Movement(Direction direction, Type type, PaymentEntry payment) {
    }
    enum Direction {
        IN, OUT
    }
    enum Type {
        PAYMENT, REFUND
    }
    record PaymentEntry(BigDecimal amount, UUID paymentMethodId, String notes,
            UUID paymentId, UUID actorId, String actorName) {
    }
}
