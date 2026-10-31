package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.PurchaseInventoryApi;
import com.cashly.cashly_api.inventory.warehouses.stock.application.purchases.services.PurchaseInventoryService;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseInventoryConfig {
    @Bean
    public PurchaseInventoryApi purchaseInventoryApi(
            WarehouseStockRepository stocks,
            StockMovementRepository movements) {
        return new PurchaseInventoryService(stocks, movements);
    }
}
