package com.cashly.cashly_api.customers.infrastructure.config;

import com.cashly.cashly_api.customers.application.api.CustomerProfileQuery;
import com.cashly.cashly_api.customers.application.queries.RepositoryCustomerProfileQuery;
import com.cashly.cashly_api.customers.domain.repositories.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProfileQueryConfig {
    @Bean
    public CustomerProfileQuery customerProfileQuery(CustomerRepository customers, CustomerBalanceRepository balances) {
        return new RepositoryCustomerProfileQuery(customers, balances);
    }
}
