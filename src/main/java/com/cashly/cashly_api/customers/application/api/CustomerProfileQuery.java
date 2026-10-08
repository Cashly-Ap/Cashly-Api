package com.cashly.cashly_api.customers.application.api;

import java.util.UUID;

public interface CustomerProfileQuery {
    CustomerProfileSnapshot find(UUID customerId);
}
