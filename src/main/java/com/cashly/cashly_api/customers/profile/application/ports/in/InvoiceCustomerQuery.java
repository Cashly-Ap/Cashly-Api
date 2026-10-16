package com.cashly.cashly_api.customers.profile.application.ports.in;
import java.util.Optional;
import java.util.UUID;
public interface InvoiceCustomerQuery {
    Optional<Customer> findCustomer(UUID customerId);
    record Customer(UUID id, String name, String rif) {}
}