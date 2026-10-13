package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.ports.in.PurchaseInventoryApi;
import com.cashly.cashly_api.warehouses.application.services.PurchaseInventoryService;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseInventoryConfig {
    @Bean
    public PurchaseInventoryApi purchaseInventoryApi(WarehouseStockRepository stocks, StockMovementRepository movements) {
        return new PurchaseInventoryService(stocks, movements);
    }
}
