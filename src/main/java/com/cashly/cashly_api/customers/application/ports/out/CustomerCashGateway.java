package com.cashly.cashly_api.customers.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerCashGateway {
    void requireOpenRegister();

    void record(CustomerCashMovement movement);

    record CustomerCashMovement(
            String direction,
            String type,
            BigDecimal amount,
            UUID paymentMethodId,
            String description,
            String sourceType,
            UUID sourceId,
            UUID actorId,
            String actorName) {}
}
