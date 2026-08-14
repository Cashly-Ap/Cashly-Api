package com.parsun.parsun_api.cashregister.domain.entities;

import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashMovementUnitTest {

    @Test
    void should_CreateIncomingMovementWithPositiveAmount() {
        CashMovement movement = CashMovement.create(
                RegisterId.generate(), CashMovementDirection.IN, CashMovementType.MANUAL_INCOME,
                new BigDecimal("25.00"), null, "Efectivo", "Ingreso adicional",
                "MANUAL", null, UUID.randomUUID(), "Operator");

        assertEquals(new BigDecimal("25.00"), movement.getAmount());
        assertEquals(CashMovementDirection.IN, movement.getDirection());
    }

    @Test
    void should_RejectNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> CashMovement.create(
                RegisterId.generate(), CashMovementDirection.OUT, CashMovementType.EXPENSE,
                BigDecimal.ZERO, null, "Efectivo", "Gasto", "MANUAL", null,
                UUID.randomUUID(), "Operator"));
    }

    @Test
    void should_RejectOutgoingIncome() {
        assertThrows(IllegalArgumentException.class, () -> CashMovement.create(
                RegisterId.generate(), CashMovementDirection.OUT, CashMovementType.MANUAL_INCOME,
                BigDecimal.ONE, null, "Efectivo", "Ingreso", "MANUAL", null,
                UUID.randomUUID(), "Operator"));
    }

    @Test
    void should_RejectIncomingExpense() {
        assertThrows(IllegalArgumentException.class, () -> CashMovement.create(
                RegisterId.generate(), CashMovementDirection.IN, CashMovementType.EXPENSE,
                BigDecimal.ONE, null, "Efectivo", "Gasto", "MANUAL", null,
                UUID.randomUUID(), "Operator"));
    }

    @Test
    void should_AllowCommercialMovementsWithTheirAccountingDirection() {
        assertDoesNotThrow(() -> CashMovement.create(
                RegisterId.generate(), CashMovementDirection.IN, CashMovementType.INVOICE,
                BigDecimal.TEN, null, null, "Factura confirmada", "INVOICE",
                UUID.randomUUID(), UUID.randomUUID(), "Tester"));
        assertDoesNotThrow(() -> CashMovement.create(
                RegisterId.generate(), CashMovementDirection.OUT, CashMovementType.CREDIT_NOTE,
                BigDecimal.ONE, null, null, "Nota de crédito", "CREDIT_NOTE",
                UUID.randomUUID(), UUID.randomUUID(), "Tester"));
    }
}
