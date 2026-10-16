package com.cashly.cashly_api.customers.profile.infrastructure.config;

import com.cashly.cashly_api.customers.profile.application.ports.in.CustomerProductPricingQuery;
import com.cashly.cashly_api.customers.profile.application.queries.RepositoryCustomerProductPricingQuery;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProductPricingConfig {
    @Bean
    public CustomerProductPricingQuery customerProductPricingQuery(CustomerRepository customers) {
        return new RepositoryCustomerProductPricingQuery(customers);
    }
}
