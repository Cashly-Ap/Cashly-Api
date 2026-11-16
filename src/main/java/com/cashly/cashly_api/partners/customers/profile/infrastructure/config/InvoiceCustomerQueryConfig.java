package com.cashly.cashly_api.partners.customers.profile.infrastructure.config;
import com.cashly.cashly_api.partners.customers.profile.application.ports.in.InvoiceCustomerQuery;
import com.cashly.cashly_api.partners.customers.profile.application.queries.services.RepositoryInvoiceCustomerQuery;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class InvoiceCustomerQueryConfig {
    @Bean
    public InvoiceCustomerQuery invoiceCustomerQuery(
            CustomerRepository customers) {
        return new RepositoryInvoiceCustomerQuery(customers);
    }
}