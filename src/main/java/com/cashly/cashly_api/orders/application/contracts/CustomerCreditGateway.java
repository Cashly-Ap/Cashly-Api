package com.cashly.cashly_api.orders.application.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerCreditGateway {
    CreditLimits getCreditLimits(UUID customerId);
    record CreditLimits(BigDecimal creditLimit, BigDecimal availableCredit) {}
}
