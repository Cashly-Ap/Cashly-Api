package com.cashly.cashly_api.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.ProductStockQuery;
import com.cashly.cashly_api.warehouses.stock.application.queries.RepositoryProductStockQuery;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductStockQueryConfig {
    @Bean
    public ProductStockQuery productStockQuery(WarehouseStockRepository stocks, WarehouseRepository warehouses) {
        return new RepositoryProductStockQuery(stocks, warehouses);
    }
}
