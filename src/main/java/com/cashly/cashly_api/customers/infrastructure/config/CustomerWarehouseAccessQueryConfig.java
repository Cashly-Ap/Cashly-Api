package com.cashly.cashly_api.customers.infrastructure.config;

import com.cashly.cashly_api.customers.application.api.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.customers.application.queries.RepositoryCustomerWarehouseAccessQuery;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerWarehouseAccessQueryConfig {
    @Bean
    public CustomerWarehouseAccessQuery customerWarehouseAccessQuery(CustomerWarehouseAccessRepository repository) {
        return new RepositoryCustomerWarehouseAccessQuery(repository);
    }
}
