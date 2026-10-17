package com.cashly.cashly_api.sales.orders.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerCreditGateway {
    CreditLimits getCreditLimits(UUID customerId);
    record CreditLimits(BigDecimal creditLimit, BigDecimal availableCredit) {}
}
