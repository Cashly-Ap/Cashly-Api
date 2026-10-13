package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.ports.in.ReturnInventoryApi;
import com.cashly.cashly_api.warehouses.application.services.ReturnInventoryService;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReturnInventoryConfig {
    @Bean
    public ReturnInventoryApi returnInventoryApi(WarehouseStockRepository stocks, StockMovementRepository movements) {
        return new ReturnInventoryService(stocks, movements);
    }
}
