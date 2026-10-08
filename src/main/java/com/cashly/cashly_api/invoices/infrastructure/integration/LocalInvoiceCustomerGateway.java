package com.cashly.cashly_api.invoices.infrastructure.integration;
import com.cashly.cashly_api.invoices.application.contracts.InvoiceCustomerGateway;
import com.cashly.cashly_api.customers.application.api.InvoiceCustomerQuery;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
@Component
public class LocalInvoiceCustomerGateway implements InvoiceCustomerGateway {
    private final InvoiceCustomerQuery customers;
    public LocalInvoiceCustomerGateway(InvoiceCustomerQuery customers) { this.customers = customers; }
    public Optional<Customer> findCustomer(UUID id) {
        return customers.findCustomer(id).map(customer -> new Customer(customer.id(), customer.name(), customer.rif()));
    }
}