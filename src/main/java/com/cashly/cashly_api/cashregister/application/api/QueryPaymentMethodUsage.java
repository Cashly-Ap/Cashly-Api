package com.cashly.cashly_api.cashregister.application.api;

import java.util.UUID;

public interface QueryPaymentMethodUsage {
    boolean hasUsage(UUID paymentMethodId);
}
