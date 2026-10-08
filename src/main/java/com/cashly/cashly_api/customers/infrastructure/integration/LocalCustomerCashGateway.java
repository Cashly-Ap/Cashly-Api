package com.cashly.cashly_api.customers.infrastructure.integration;

import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.customers.application.contracts.CustomerCashGateway;
import org.springframework.stereotype.Component;

@Component
public class LocalCustomerCashGateway implements CustomerCashGateway {
    private final CashMovementService cashMovements;

    public LocalCustomerCashGateway(CashMovementService cashMovements) {
        this.cashMovements = cashMovements;
    }

    @Override
    public void requireOpenRegister() {
        cashMovements.requireOpenRegister();
    }

    @Override
    public void record(CustomerCashMovement movement) {
        cashMovements.record(
                CashMovementDirection.valueOf(movement.direction()),
                CashMovementType.valueOf(movement.type()),
                movement.amount(), movement.paymentMethodId(), movement.description(),
                movement.sourceType(), movement.sourceId(), movement.actorId(), movement.actorName());
    }
}
