package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.customers.application.api.CustomerCreditTermsQuery;
import com.cashly.cashly_api.invoices.application.contracts.CustomerCreditTermsGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LocalCustomerCreditTermsGateway implements CustomerCreditTermsGateway {
    private final CustomerCreditTermsQuery query;

    public LocalCustomerCreditTermsGateway(CustomerCreditTermsQuery query) {
        this.query = query;
    }

    @Override
    public int creditDays(UUID customerId) {
        return query.creditDays(customerId);
    }
}
