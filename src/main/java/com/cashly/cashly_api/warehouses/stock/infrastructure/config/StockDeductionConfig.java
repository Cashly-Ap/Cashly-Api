package com.cashly.cashly_api.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.StockDeductionApi;
import com.cashly.cashly_api.warehouses.stock.application.deduction.services.StockDeductionService;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockDeductionConfig {
    @Bean
    public StockDeductionApi stockDeductionApi(WarehouseStockRepository stocks) {
        return new StockDeductionService(stocks);
    }
}
