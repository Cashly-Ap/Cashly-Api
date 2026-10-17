package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface CashRegisterPaymentMethodGateway {
    Optional<PaymentMethod> findById(UUID paymentMethodId);

    record PaymentMethod(UUID id, String name) {}
}
