package com.cashly.cashly_api.cash_register.application.ports.in;

import java.util.UUID;

public interface QueryPaymentMethodUsage {
    boolean hasUsage(UUID paymentMethodId);
}
