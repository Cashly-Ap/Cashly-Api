package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.in;

import java.util.UUID;

public interface QueryPaymentMethodUsage {
    boolean hasUsage(UUID paymentMethodId);
}
