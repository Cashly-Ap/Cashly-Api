package com.cashly.cashly_api.cashregister.infrastructure.persistence;

import com.cashly.cashly_api.cashregister.domain.entities.CashMovement;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.cashregister.domain.valueobjects.RegisterId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class CashRegisterRepositoryAdapterSessionTotalsTest {

    @Test
    void should_SeparateNetInvoicingFromCashFlow() {
        RegisterId registerId = RegisterId.generate();
        UUID paymentMethodId = UUID.randomUUID();
        CashRegisterRepositoryAdapter adapter = spy(new CashRegisterRepositoryAdapter(
                mock(CashRegisterJpaRepository.class),
                mock(EntityManager.class),
                mock(CashMovementJpaRepository.class)
        ));
        doReturn(List.of(
                movement(registerId, CashMovementDirection.IN, CashMovementType.INVOICE, "100.00", null),
                movement(registerId, CashMovementDirection.IN, CashMovementType.PAYMENT, "40.00", paymentMethodId),
                movement(registerId, CashMovementDirection.IN, CashMovementType.DEBIT_NOTE, "20.00", null),
                movement(registerId, CashMovementDirection.OUT, CashMovementType.CREDIT_NOTE, "10.00", null),
                movement(registerId, CashMovementDirection.OUT, CashMovementType.RETURN, "15.00", null),
                movement(registerId, CashMovementDirection.OUT, CashMovementType.EXPENSE, "5.00", paymentMethodId)
        )).when(adapter).findMovements(registerId);

        var totals = adapter.findSessionTotals(registerId);

        assertEquals(new BigDecimal("95.00"), totals.totalInvoiced());
        assertEquals(new BigDecimal("100.00"), totals.totalInvoices());
        assertEquals(new BigDecimal("20.00"), totals.totalDebitNotes());
        assertEquals(new BigDecimal("10.00"), totals.totalCreditNotes());
        assertEquals(new BigDecimal("15.00"), totals.totalReturns());
        assertEquals(new BigDecimal("40.00"), totals.totalRevenue());
        assertEquals(new BigDecimal("5.00"), totals.totalExpenses());
    }

    private CashMovement movement(RegisterId registerId, CashMovementDirection direction,
                                  CashMovementType type, String amount, UUID paymentMethodId) {
        return CashMovement.reconstitute(
                UUID.randomUUID(), registerId, direction, type, new BigDecimal(amount),
                paymentMethodId, paymentMethodId == null ? null : "Efectivo",
                type.name(), type.name(), UUID.randomUUID(), UUID.randomUUID(), "Tester",
                LocalDateTime.now()
        );
    }
}
