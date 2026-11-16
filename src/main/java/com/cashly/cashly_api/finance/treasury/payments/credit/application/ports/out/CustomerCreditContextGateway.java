package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out;

import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.CustomerCreditContext;
import java.util.Optional;
import java.util.UUID;
public interface CustomerCreditContextGateway {
    Optional<CustomerCreditContext> findById(UUID customerId);
}
