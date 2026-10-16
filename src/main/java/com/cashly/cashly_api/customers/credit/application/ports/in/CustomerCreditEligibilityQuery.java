package com.cashly.cashly_api.customers.credit.application.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerCreditEligibilityQuery {
    boolean isCreditAvailable(UUID customerId, BigDecimal amount);
    CustomerCreditLimits getCreditLimits(UUID customerId);
}
