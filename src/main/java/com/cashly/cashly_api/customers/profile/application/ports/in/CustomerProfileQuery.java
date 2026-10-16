package com.cashly.cashly_api.customers.profile.application.ports.in;

import java.util.UUID;

public interface CustomerProfileQuery {
    CustomerProfileSnapshot find(UUID customerId);
}
