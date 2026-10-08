package com.cashly.cashly_api.payment_methods.application.usecases;

import com.cashly.cashly_api.payment_methods.infrastructure.persistence.repositories.PaymentMethodRepositoryAdapter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodBoundaryTest {
    @Test
    void should_consult_usage_through_application_contract() {
        var constructor = DeletePaymentMethodUseCase.class.getConstructors()[0];
        assertEquals(2, constructor.getParameterCount());
        assertEquals("com.cashly.cashly_api.payment_methods.application.contracts.PaymentMethodUsageGateway",
                constructor.getParameterTypes()[1].getName());
    }

    @Test
    void should_keep_payment_method_persistence_independent_of_foreign_repositories() {
        var constructor = PaymentMethodRepositoryAdapter.class.getConstructors()[0];
        assertEquals(1, constructor.getParameterCount());
    }
}
