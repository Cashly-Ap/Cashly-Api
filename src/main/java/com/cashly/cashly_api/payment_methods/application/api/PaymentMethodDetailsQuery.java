package com.cashly.cashly_api.payment_methods.application.api;

import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodDetailsQuery {
    Optional<PaymentMethodDetails> findById(UUID paymentMethodId);

    record PaymentMethodDetails(UUID id, String name) {}
}
