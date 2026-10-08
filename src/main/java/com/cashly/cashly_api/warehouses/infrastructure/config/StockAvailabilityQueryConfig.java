package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.api.StockAvailabilityQuery;
import com.cashly.cashly_api.warehouses.application.contracts.CustomerWarehouseAccessGateway;
import com.cashly.cashly_api.warehouses.application.services.StockAvailabilityQueryService;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockAvailabilityQueryConfig {
    @Bean
    public StockAvailabilityQuery stockAvailabilityQuery(WarehouseStockRepository stockRepository,
                                                         WarehouseRepository warehouseRepository,
                                                         CustomerWarehouseAccessGateway accessGateway) {
        return new StockAvailabilityQueryService(stockRepository, warehouseRepository, accessGateway);
    }
}
