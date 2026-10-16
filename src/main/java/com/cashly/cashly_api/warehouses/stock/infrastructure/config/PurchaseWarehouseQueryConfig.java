package com.cashly.cashly_api.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.PurchaseWarehouseQuery;
import com.cashly.cashly_api.warehouses.stock.application.queries.services.RepositoryPurchaseWarehouseQuery;
import com.cashly.cashly_api.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseWarehouseQueryConfig {
    @Bean
    public PurchaseWarehouseQuery purchaseWarehouseQuery(WarehouseRepository warehouses, WarehouseStockRepository stocks) {
        return new RepositoryPurchaseWarehouseQuery(warehouses, stocks);
    }
}