package com.cashly.cashly_api.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.ReturnInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.returns.ReturnInventoryService;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReturnInventoryConfig {
    @Bean
    public ReturnInventoryApi returnInventoryApi(WarehouseStockRepository stocks, StockMovementRepository movements) {
        return new ReturnInventoryService(stocks, movements);
    }
}
