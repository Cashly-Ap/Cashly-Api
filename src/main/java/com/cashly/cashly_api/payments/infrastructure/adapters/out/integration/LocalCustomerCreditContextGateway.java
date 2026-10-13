package com.cashly.cashly_api.payments.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.payments.application.ports.out.model.CustomerCreditContext;
import com.cashly.cashly_api.payments.application.ports.out.CustomerCreditContextGateway;
import com.cashly.cashly_api.customers.application.ports.in.CustomerCreditContextQuery;
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
