package com.cashly.cashly_api.customers.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.cash_register.application.services.CashMovementService;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.customers.application.ports.out.CustomerCashGateway.CustomerCashMovement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LocalCustomerCashGatewayTest {
    @Test
    void maps_customer_cash_contract_to_cash_register_api() {
        CashMovementService cash = mock(CashMovementService.class);
        var gateway = new LocalCustomerCashGateway(cash);
        var movement = new CustomerCashMovement("IN", "ADVANCE", new BigDecimal("25.00"),
                UUID.randomUUID(), "Advance", "CUSTOMER_BALANCE", UUID.randomUUID(), UUID.randomUUID(), "Operator");

        gateway.record(movement);

        verify(cash).record(CashMovementDirection.IN, CashMovementType.ADVANCE, movement.amount(),
                movement.paymentMethodId(), movement.description(), movement.sourceType(), movement.sourceId(),
                movement.actorId(), movement.actorName());
    }
}
