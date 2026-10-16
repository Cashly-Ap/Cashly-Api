package com.cashly.cashly_api.payment_methods.infrastructure.integration;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalPaymentMethodUsageGatewayTest {
    private final com.cashly.cashly_api.payments.collection.application.ports.in.QueryPaymentMethodUsage payments =
            mock(com.cashly.cashly_api.payments.collection.application.ports.in.QueryPaymentMethodUsage.class);
    private final com.cashly.cashly_api.cash_register.sessions.application.ports.in.QueryPaymentMethodUsage cashregister =
            mock(com.cashly.cashly_api.cash_register.sessions.application.ports.in.QueryPaymentMethodUsage.class);
    private final LocalPaymentMethodUsageGateway gateway = new LocalPaymentMethodUsageGateway(payments, cashregister);

    @Test
    void should_ReturnTrue_When_PaymentExists() {
        UUID id = UUID.randomUUID();
        when(payments.hasUsage(id)).thenReturn(true);
        assertTrue(gateway.hasAssociatedRecords(id));
        verifyNoInteractions(cashregister);
    }

    @Test
    void should_ReturnTrue_When_CashRegisterDetailExists() {
        UUID id = UUID.randomUUID();
        when(payments.hasUsage(id)).thenReturn(false);
        when(cashregister.hasUsage(id)).thenReturn(true);
        assertTrue(gateway.hasAssociatedRecords(id));
    }

    @Test
    void should_ReturnFalse_When_NoAssociatedRecords() {
        UUID id = UUID.randomUUID();
        when(payments.hasUsage(id)).thenReturn(false);
        when(cashregister.hasUsage(id)).thenReturn(false);
        assertFalse(gateway.hasAssociatedRecords(id));
    }
}
