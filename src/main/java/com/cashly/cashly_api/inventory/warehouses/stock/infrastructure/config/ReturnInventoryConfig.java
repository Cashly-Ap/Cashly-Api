package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.ReturnInventoryApi;
import com.cashly.cashly_api.inventory.warehouses.stock.application.returns.services.ReturnInventoryService;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReturnInventoryConfig {
    @Bean
    public ReturnInventoryApi returnInventoryApi(
            WarehouseStockRepository stocks,
            StockMovementRepository movements) {
        return new ReturnInventoryService(stocks, movements);
    }
}
