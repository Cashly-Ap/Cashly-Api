package com.cashly.cashly_api.customers.profile.infrastructure.config;

import com.cashly.cashly_api.customers.profile.application.ports.in.CustomerProfileQuery;
import com.cashly.cashly_api.customers.profile.application.queries.RepositoryCustomerProfileQuery;
import com.cashly.cashly_api.customers.balance.domain.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProfileQueryConfig {
    @Bean
    public CustomerProfileQuery customerProfileQuery(CustomerRepository customers, CustomerBalanceRepository balances) {
        return new RepositoryCustomerProfileQuery(customers, balances);
    }
}
