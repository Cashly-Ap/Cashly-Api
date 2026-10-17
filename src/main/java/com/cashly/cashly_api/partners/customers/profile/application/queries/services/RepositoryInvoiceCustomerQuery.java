package com.cashly.cashly_api.partners.customers.profile.application.queries.services;
import com.cashly.cashly_api.partners.customers.profile.application.ports.in.InvoiceCustomerQuery;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
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