package com.cashly.cashly_api.payments.application.ports.in;

import java.util.UUID;

public interface QueryPaymentMethodUsage {
    boolean hasUsage(UUID paymentMethodId);
}
