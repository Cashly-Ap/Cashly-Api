package com.cashly.cashly_api.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.PurchaseInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.purchases.PurchaseInventoryService;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseInventoryConfig {
    @Bean
    public PurchaseInventoryApi purchaseInventoryApi(WarehouseStockRepository stocks, StockMovementRepository movements) {
        return new PurchaseInventoryService(stocks, movements);
    }
}
