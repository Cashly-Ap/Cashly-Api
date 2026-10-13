package com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.customers.application.ports.in.CustomerCreditTermsQuery;
import com.cashly.cashly_api.invoices.application.ports.out.CustomerCreditTermsGateway;
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
