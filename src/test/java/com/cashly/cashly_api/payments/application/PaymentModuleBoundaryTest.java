package com.cashly.cashly_api.payments.application;

import com.cashly.cashly_api.payments.application.usecases.ApprovePaymentUseCase;
import com.cashly.cashly_api.payments.application.usecases.CreatePaymentUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayAllCustomerDebtUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayInvoiceUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayPartialDebtUseCase;
import com.cashly.cashly_api.payments.application.usecases.RejectPaymentUseCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentModuleBoundaryTest {
    @Test
    void payment_use_cases_depend_only_on_payment_module_types() {
        Class<?>[] useCases = {CreatePaymentUseCase.class, PayInvoiceUseCase.class,
                PayAllCustomerDebtUseCase.class, PayPartialDebtUseCase.class,
                ApprovePaymentUseCase.class, RejectPaymentUseCase.class};

        for (Class<?> useCase : useCases) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type ->
                                type.getPackageName().startsWith("com.cashly.cashly_api.payments.")),
                        useCase.getSimpleName() + " imports an external module into its constructor");
            }
        }
    }
}
