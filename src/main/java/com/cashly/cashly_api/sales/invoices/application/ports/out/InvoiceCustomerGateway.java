package com.cashly.cashly_api.sales.invoices.application.ports.out;
import java.util.Optional;
import java.util.UUID;
public interface InvoiceCustomerGateway {
    Optional<Customer> findCustomer(UUID customerId);
    record Customer(UUID id, String name, String rif) {
    }
}