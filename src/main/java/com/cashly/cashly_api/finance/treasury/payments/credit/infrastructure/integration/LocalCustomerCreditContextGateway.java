package com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.CustomerCreditContext;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CustomerCreditContextGateway;
import com.cashly.cashly_api.customers.credit.application.ports.in.CustomerCreditContextQuery;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
@Component
public class LocalCustomerCreditContextGateway implements CustomerCreditContextGateway {
    private final CustomerCreditContextQuery query;
    public LocalCustomerCreditContextGateway(CustomerCreditContextQuery query) { this.query = query; }
    public Optional<CustomerCreditContext> findById(UUID id) {
        return query.findById(id).map(entry -> new CustomerCreditContext(entry.customerId(), entry.name(), entry.rif(), entry.creditLimit(), entry.balance()));
    }
}
