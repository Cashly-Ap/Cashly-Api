package com.cashly.cashly_api.payments.application.ports.out;

import com.cashly.cashly_api.payments.application.ports.out.model.CustomerCreditContext;
import java.util.Optional;
import java.util.UUID;
public interface CustomerCreditContextGateway {
    Optional<CustomerCreditContext> findById(UUID customerId);
}
