package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.PurchaseWarehouseQuery;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.services.RepositoryPurchaseWarehouseQuery;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseWarehouseQueryConfig {
    @Bean
    public PurchaseWarehouseQuery purchaseWarehouseQuery(WarehouseRepository warehouses, WarehouseStockRepository stocks) {
        return new RepositoryPurchaseWarehouseQuery(warehouses, stocks);
    }
}