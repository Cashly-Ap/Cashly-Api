package com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in;

import java.util.UUID;

public interface QueryPaymentMethodUsage {
    boolean hasUsage(UUID paymentMethodId);
}
