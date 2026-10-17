package com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.out.PaymentMethodUsageGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.repositories.PaymentJpaRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.repositories.CashRegisterJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentMethodUsageWiringTest {
    @Test
    void should_wire_both_module_queries_without_component_name_collision() {
        PaymentJpaRepository payments = mock(PaymentJpaRepository.class);
        CashRegisterJpaRepository cashregister = mock(CashRegisterJpaRepository.class);
        UUID id = UUID.randomUUID();
        when(payments.countByPaymentMethodId(id)).thenReturn(0L);
        when(cashregister.countDetailsByPaymentMethodId(id)).thenReturn(1L);
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(PaymentJpaRepository.class, () -> payments);
            context.registerBean(CashRegisterJpaRepository.class, () -> cashregister);
            context.register(com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.queries.JpaPaymentMethodUsageQuery.class,
                    com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.queries.JpaPaymentMethodUsageQuery.class,
                    LocalPaymentMethodUsageGateway.class);
            context.refresh();
            assertTrue(context.getBean(PaymentMethodUsageGateway.class).hasAssociatedRecords(id));
            assertTrue(context.containsBean("paymentsPaymentMethodUsageQuery"));
            assertTrue(context.containsBean("cashregisterPaymentMethodUsageQuery"));
        }
    }
}
