package com.cashly.cashly_api.customers.infrastructure.config;

import com.cashly.cashly_api.customers.application.ports.in.CustomerCreditTermsQuery;
import com.cashly.cashly_api.customers.application.queries.RepositoryCustomerCreditTermsQuery;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerCreditTermsConfig {
    @Bean
    public CustomerCreditTermsQuery customerCreditTermsQuery(CustomerRepository customers) {
        return new RepositoryCustomerCreditTermsQuery(customers);
    }
}
