package com.cashly.cashly_api.invoices.application.ports.out;

import java.util.UUID;

public interface CustomerCreditTermsGateway {
    int creditDays(UUID customerId);
}
