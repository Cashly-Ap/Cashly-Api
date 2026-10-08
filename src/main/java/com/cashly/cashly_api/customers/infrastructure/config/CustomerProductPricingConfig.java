package com.cashly.cashly_api.customers.infrastructure.config;

import com.cashly.cashly_api.customers.application.api.CustomerProductPricingQuery;
import com.cashly.cashly_api.customers.application.services.RepositoryCustomerProductPricingQuery;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProductPricingConfig {
    @Bean
    public CustomerProductPricingQuery customerProductPricingQuery(CustomerRepository customers) {
        return new RepositoryCustomerProductPricingQuery(customers);
    }
}
