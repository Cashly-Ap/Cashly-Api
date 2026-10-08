package com.cashly.cashly_api.invoices.application.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerCreditGateway {
    boolean isCreditAvailable(UUID customerId, BigDecimal amount);
}
