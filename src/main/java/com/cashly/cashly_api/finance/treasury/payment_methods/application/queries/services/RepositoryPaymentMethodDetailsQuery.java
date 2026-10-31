package com.cashly.cashly_api.finance.treasury.payment_methods.application.queries.services;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.in.PaymentMethodDetailsQuery;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.repository.PaymentMethodRepository;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.valueobjects.PaymentMethodId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RepositoryPaymentMethodDetailsQuery
        implements
            PaymentMethodDetailsQuery {
    private final PaymentMethodRepository paymentMethods;

    public RepositoryPaymentMethodDetailsQuery(
            PaymentMethodRepository paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    @Override
    public Optional<PaymentMethodDetails> findById(UUID paymentMethodId) {
        return paymentMethods.findById(PaymentMethodId.of(paymentMethodId))
                .map(method -> new PaymentMethodDetails(method.getId().value(),
                        method.getName()));
    }
}
