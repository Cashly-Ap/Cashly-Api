package com.cashly.cashly_api.partners.customers.profile.infrastructure.config;

import com.cashly.cashly_api.partners.customers.profile.application.ports.in.CustomerProfileQuery;
import com.cashly.cashly_api.partners.customers.profile.application.queries.services.RepositoryCustomerProfileQuery;
import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProfileQueryConfig {
    @Bean
    public CustomerProfileQuery customerProfileQuery(
            CustomerRepository customers, CustomerBalanceRepository balances) {
        return new RepositoryCustomerProfileQuery(customers, balances);
    }
}
