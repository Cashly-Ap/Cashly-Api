package com.cashly.cashly_api.partners.customers.profile.application.ports.in;

import java.util.UUID;

public interface CustomerCreditTermsQuery {
    int creditDays(UUID customerId);
}
