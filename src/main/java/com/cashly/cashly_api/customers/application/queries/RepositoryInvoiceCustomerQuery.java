package com.cashly.cashly_api.customers.application.queries;
import com.cashly.cashly_api.customers.application.ports.in.InvoiceCustomerQuery;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import java.util.Optional;
import java.util.UUID;
public class RepositoryInvoiceCustomerQuery implements InvoiceCustomerQuery {
    private final CustomerRepository customers;
    public RepositoryInvoiceCustomerQuery(CustomerRepository customers) { this.customers = customers; }
    public Optional<Customer> findCustomer(UUID id) {
        return customers.findById(new CustomerId(id)).map(customer -> new Customer(customer.getId().getValue(),
                customer.getLegalName().getValue(), customer.getRif().getValue()));
    }
}