package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;

import com.cashly.cashly_api.customers.profile.application.ports.in.CustomerCreditTermsQuery;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditTermsGateway;
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
