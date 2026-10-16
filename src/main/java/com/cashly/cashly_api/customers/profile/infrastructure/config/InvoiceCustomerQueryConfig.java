package com.cashly.cashly_api.customers.profile.infrastructure.config;
import com.cashly.cashly_api.customers.profile.application.ports.in.InvoiceCustomerQuery;
import com.cashly.cashly_api.customers.profile.application.queries.RepositoryInvoiceCustomerQuery;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class InvoiceCustomerQueryConfig {
    @Bean
    public InvoiceCustomerQuery invoiceCustomerQuery(CustomerRepository customers) {
        return new RepositoryInvoiceCustomerQuery(customers);
    }
}