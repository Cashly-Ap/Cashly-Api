package com.cashly.cashly_api.customers.warehouse_access.infrastructure.config;

import com.cashly.cashly_api.customers.warehouse_access.application.ports.in.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.customers.warehouse_access.application.queries.RepositoryCustomerWarehouseAccessQuery;
import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccessRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerWarehouseAccessQueryConfig {
    @Bean
    public CustomerWarehouseAccessQuery customerWarehouseAccessQuery(CustomerWarehouseAccessRepository repository) {
        return new RepositoryCustomerWarehouseAccessQuery(repository);
    }
}
