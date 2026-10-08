package com.cashly.cashly_api.invoices.application.contracts;

import java.util.UUID;

public interface CustomerCreditTermsGateway {
    int creditDays(UUID customerId);
}
