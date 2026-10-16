package com.cashly.cashly_api.customers.balance.infrastructure.integration;

import com.cashly.cashly_api.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.customers.balance.application.ports.out.CustomerCashGateway;
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
