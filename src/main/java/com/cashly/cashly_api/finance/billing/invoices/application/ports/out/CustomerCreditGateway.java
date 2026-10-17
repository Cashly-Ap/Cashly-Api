package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerCreditGateway {
    boolean isCreditAvailable(UUID customerId, BigDecimal amount);
}
