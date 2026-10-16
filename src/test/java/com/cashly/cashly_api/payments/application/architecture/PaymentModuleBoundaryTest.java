package com.cashly.cashly_api.payments.application.architecture;

import com.cashly.cashly_api.payments.collection.application.approve.use_case.ApprovePaymentUseCase;
import com.cashly.cashly_api.payments.collection.application.create.use_case.CreatePaymentUseCase;
import com.cashly.cashly_api.payments.collection.application.pay_customer_debt.use_case.PayAllCustomerDebtUseCase;
import com.cashly.cashly_api.payments.collection.application.pay_invoice.use_case.PayInvoiceUseCase;
import com.cashly.cashly_api.payments.collection.application.pay_partial_debt.use_case.PayPartialDebtUseCase;
import com.cashly.cashly_api.payments.collection.application.reject.use_case.RejectPaymentUseCase;
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
