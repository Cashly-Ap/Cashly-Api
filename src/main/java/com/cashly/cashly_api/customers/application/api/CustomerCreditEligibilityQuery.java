package com.cashly.cashly_api.customers.application.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerCreditEligibilityQuery {
    boolean isCreditAvailable(UUID customerId, BigDecimal amount);
    CustomerCreditLimits getCreditLimits(UUID customerId);
}
