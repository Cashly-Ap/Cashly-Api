package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LocalPaymentCashRegisterGatewayTest {
    @Test
    void refund_movement_preserves_outflow_type_and_payment_reference() {
        CashMovementService cashMovements = mock(CashMovementService.class);
        LocalPaymentCashRegisterGateway gateway = new LocalPaymentCashRegisterGateway(cashMovements);
        UUID paymentId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID methodId = UUID.randomUUID();

        gateway.recordMovement(new CashRegisterGateway.Movement(CashRegisterGateway.Direction.OUT,
                CashRegisterGateway.Type.REFUND, new CashRegisterGateway.PaymentEntry(
                new BigDecimal("32.40"), methodId, "Refund", paymentId, actorId, "Manager")));

        verify(cashMovements).record(CashMovementDirection.OUT, CashMovementType.REFUND,
                new BigDecimal("32.40"), methodId, "Refund", "PAYMENT", paymentId, actorId, "Manager");
    }
}
