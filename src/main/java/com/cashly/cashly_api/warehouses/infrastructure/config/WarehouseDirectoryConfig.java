package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.api.WarehouseDirectoryQuery;
import com.cashly.cashly_api.warehouses.application.services.RepositoryWarehouseDirectoryQuery;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseDirectoryConfig {
    @Bean
    public WarehouseDirectoryQuery warehouseDirectoryQuery(WarehouseRepository warehouses) {
        return new RepositoryWarehouseDirectoryQuery(warehouses);
    }
}
