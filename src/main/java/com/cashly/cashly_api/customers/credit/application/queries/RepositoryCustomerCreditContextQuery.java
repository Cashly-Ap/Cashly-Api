package com.cashly.cashly_api.customers.credit.application.queries;
import com.cashly.cashly_api.customers.credit.application.ports.in.CustomerCreditContextQuery;
import com.cashly.cashly_api.customers.credit.application.ports.in.CustomerCreditContextEntry;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.customers.balance.domain.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
public class RepositoryCustomerCreditContextQuery implements CustomerCreditContextQuery {
    private final CustomerRepository customers;
    private final CustomerBalanceRepository balances;
    public RepositoryCustomerCreditContextQuery(CustomerRepository customers, CustomerBalanceRepository balances) {
        this.customers = customers;
        this.balances = balances;
    }
    public Optional<CustomerCreditContextEntry> findById(UUID id) {
        return customers.findById(new CustomerId(id)).map(customer -> new CustomerCreditContextEntry(
                id, customer.getLegalName().getValue(), customer.getRif().getValue(), customer.getCreditLimit().getValue(),
                balances.findByCustomerId(id).map(balance -> balance.getTotalBalance()).orElse(BigDecimal.ZERO)));
    }
}
