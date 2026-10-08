package com.cashly.cashly_api.customers.application.api;

import java.util.UUID;

public interface CustomerCreditTermsQuery {
    int creditDays(UUID customerId);
}
