package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.ports.in.StockDeductionApi;
import com.cashly.cashly_api.warehouses.application.services.StockDeductionService;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockDeductionConfig {
    @Bean
    public StockDeductionApi stockDeductionApi(WarehouseStockRepository stocks) {
        return new StockDeductionService(stocks);
    }
}
