package com.cashly.cashly_api.cash_register.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterPaymentMethodGateway;
import com.cashly.cashly_api.payment_methods.application.ports.in.PaymentMethodDetailsQuery;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalCashRegisterPaymentMethodGateway implements CashRegisterPaymentMethodGateway {
    private final PaymentMethodDetailsQuery paymentMethods;

    public LocalCashRegisterPaymentMethodGateway(PaymentMethodDetailsQuery paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    @Override
    public Optional<PaymentMethod> findById(UUID paymentMethodId) {
        return paymentMethods.findById(paymentMethodId)
                .map(method -> new PaymentMethod(method.id(), method.name()));
    }
}
