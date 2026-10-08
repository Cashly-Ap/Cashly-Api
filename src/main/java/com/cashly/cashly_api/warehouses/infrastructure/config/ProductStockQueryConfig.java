package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.api.ProductStockQuery;
import com.cashly.cashly_api.warehouses.application.services.RepositoryProductStockQuery;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductStockQueryConfig {
    @Bean
    public ProductStockQuery productStockQuery(WarehouseStockRepository stocks, WarehouseRepository warehouses) {
        return new RepositoryProductStockQuery(stocks, warehouses);
    }
}
