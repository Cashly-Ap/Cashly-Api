package com.cashly.cashly_api.customers.application.ports.in;

import java.util.UUID;

public interface CustomerCreditTermsQuery {
    int creditDays(UUID customerId);
}
