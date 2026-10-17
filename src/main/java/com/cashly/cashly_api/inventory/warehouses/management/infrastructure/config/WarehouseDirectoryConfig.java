package com.cashly.cashly_api.inventory.warehouses.management.infrastructure.config;

import com.cashly.cashly_api.inventory.warehouses.management.application.ports.in.WarehouseDirectoryQuery;
import com.cashly.cashly_api.inventory.warehouses.management.application.queries.services.RepositoryWarehouseDirectoryQuery;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseDirectoryConfig {
    @Bean
    public WarehouseDirectoryQuery warehouseDirectoryQuery(WarehouseRepository warehouses) {
        return new RepositoryWarehouseDirectoryQuery(warehouses);
    }
}
