package com.cashly.cashly_api.orders.infrastructure.integration;

import com.cashly.cashly_api.customers.credit.application.ports.in.CustomerCreditEligibilityQuery;
import com.cashly.cashly_api.orders.application.ports.out.CustomerCreditGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class OrderCustomerCreditAdapter implements CustomerCreditGateway {
    private final CustomerCreditEligibilityQuery query;

    public OrderCustomerCreditAdapter(CustomerCreditEligibilityQuery query) {
        this.query = query;
    }

    @Override
    public CreditLimits getCreditLimits(UUID customerId) {
        var limits = query.getCreditLimits(customerId);
        return new CreditLimits(limits.creditLimit(), limits.availableCredit());
    }
}
