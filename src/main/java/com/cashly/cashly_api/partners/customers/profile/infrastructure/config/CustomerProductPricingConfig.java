package com.cashly.cashly_api.partners.customers.profile.infrastructure.config;

import com.cashly.cashly_api.partners.customers.profile.application.ports.in.CustomerProductPricingQuery;
import com.cashly.cashly_api.partners.customers.profile.application.queries.services.RepositoryCustomerProductPricingQuery;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProductPricingConfig {
    @Bean
    public CustomerProductPricingQuery customerProductPricingQuery(
            CustomerRepository customers) {
        return new RepositoryCustomerProductPricingQuery(customers);
    }
}
