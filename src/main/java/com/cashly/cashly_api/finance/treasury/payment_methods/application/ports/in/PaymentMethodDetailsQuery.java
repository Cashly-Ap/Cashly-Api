package com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.in;

import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodDetailsQuery {
    Optional<PaymentMethodDetails> findById(UUID paymentMethodId);

    record PaymentMethodDetails(UUID id, String name) {
    }
}
