package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.customers.application.api.CustomerCreditEligibilityQuery;
import com.cashly.cashly_api.invoices.application.contracts.CustomerCreditGateway;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class InvoiceCustomerCreditAdapter implements CustomerCreditGateway {
    private final CustomerCreditEligibilityQuery query;

    public InvoiceCustomerCreditAdapter(CustomerCreditEligibilityQuery query) {
        this.query = query;
    }

    @Override
    public boolean isCreditAvailable(UUID customerId, BigDecimal amount) {
        return query.isCreditAvailable(customerId, amount);
    }
}
