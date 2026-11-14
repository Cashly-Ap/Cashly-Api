package com.cashly.cashly_api.sales.invoices.application.ports.out;

import java.util.UUID;

public interface CustomerCreditTermsGateway {
    int creditDays(UUID customerId);
}
