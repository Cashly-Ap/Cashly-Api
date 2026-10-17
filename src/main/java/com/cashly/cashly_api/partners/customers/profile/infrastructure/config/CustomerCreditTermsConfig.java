package com.cashly.cashly_api.partners.customers.profile.infrastructure.config;

import com.cashly.cashly_api.partners.customers.profile.application.ports.in.CustomerCreditTermsQuery;
import com.cashly.cashly_api.partners.customers.profile.application.queries.services.RepositoryCustomerCreditTermsQuery;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerCreditTermsConfig {
    @Bean
    public CustomerCreditTermsQuery customerCreditTermsQuery(CustomerRepository customers) {
        return new RepositoryCustomerCreditTermsQuery(customers);
    }
}
