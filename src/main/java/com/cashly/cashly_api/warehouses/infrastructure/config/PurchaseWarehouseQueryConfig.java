package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.ports.in.PurchaseWarehouseQuery;
import com.cashly.cashly_api.warehouses.application.queries.RepositoryPurchaseWarehouseQuery;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseWarehouseQueryConfig {
    @Bean
    public PurchaseWarehouseQuery purchaseWarehouseQuery(WarehouseRepository warehouses, WarehouseStockRepository stocks) {
        return new RepositoryPurchaseWarehouseQuery(warehouses, stocks);
    }
}