package com.cashly.cashly_api.warehouses.stock.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.StockAvailabilityQuery;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.CustomerWarehouseAccessGateway;
import com.cashly.cashly_api.warehouses.stock.application.queries.services.StockAvailabilityQueryService;
import com.cashly.cashly_api.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
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
