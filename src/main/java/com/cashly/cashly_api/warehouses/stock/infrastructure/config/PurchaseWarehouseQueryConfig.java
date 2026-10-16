package com.cashly.cashly_api.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.PurchaseWarehouseQuery;
import com.cashly.cashly_api.warehouses.stock.application.queries.RepositoryPurchaseWarehouseQuery;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseWarehouseQueryConfig {
    @Bean
    public PurchaseWarehouseQuery purchaseWarehouseQuery(WarehouseRepository warehouses, WarehouseStockRepository stocks) {
        return new RepositoryPurchaseWarehouseQuery(warehouses, stocks);
    }
}