package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockOverviewQueryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case.GetStockOverviewUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockOverviewConfig {
    @Bean
    public GetStockOverviewUseCase getStockOverviewUseCase(StockOverviewQueryGateway gateway) {
        return new GetStockOverviewUseCase(gateway);
    }
}
