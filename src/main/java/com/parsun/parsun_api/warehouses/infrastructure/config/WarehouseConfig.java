package com.parsun.parsun_api.warehouses.infrastructure.config;

import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.application.usecases.*;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseConfig {

    @Bean
    public CreateWarehouseUseCase createWarehouseUseCase(WarehouseRepository warehouseRepository) {
        return new CreateWarehouseUseCase(warehouseRepository);
    }

    @Bean
    public GetWarehouseUseCase getWarehouseUseCase(WarehouseRepository warehouseRepository) {
        return new GetWarehouseUseCase(warehouseRepository);
    }

    @Bean
    public ListWarehousesUseCase listWarehousesUseCase(WarehouseRepository warehouseRepository) {
        return new ListWarehousesUseCase(warehouseRepository);
    }

    @Bean
    public UpdateWarehouseUseCase updateWarehouseUseCase(WarehouseRepository warehouseRepository) {
        return new UpdateWarehouseUseCase(warehouseRepository);
    }

    @Bean
    public DeleteWarehouseUseCase deleteWarehouseUseCase(WarehouseRepository warehouseRepository) {
        return new DeleteWarehouseUseCase(warehouseRepository);
    }

    @Bean
    public StockEntryUseCase stockEntryUseCase(WarehouseRepository warehouseRepository,
                                                WarehouseStockRepository warehouseStockRepository,
                                                StockMovementRepository stockMovementRepository,
                                                ProductRepository productRepository) {
        return new StockEntryUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, productRepository);
    }

    @Bean
    public StockExitUseCase stockExitUseCase(WarehouseRepository warehouseRepository,
                                              WarehouseStockRepository warehouseStockRepository,
                                              StockMovementRepository stockMovementRepository,
                                              ProductRepository productRepository) {
        return new StockExitUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, productRepository);
    }

    @Bean
    public StockAdjustmentUseCase stockAdjustmentUseCase(WarehouseRepository warehouseRepository,
                                                          WarehouseStockRepository warehouseStockRepository,
                                                          StockMovementRepository stockMovementRepository,
                                                          ProductRepository productRepository) {
        return new StockAdjustmentUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, productRepository);
    }

    @Bean
    public TransferStockUseCase transferStockUseCase(WarehouseRepository warehouseRepository,
                                                      WarehouseStockRepository warehouseStockRepository,
                                                      StockMovementRepository stockMovementRepository,
                                                      ProductRepository productRepository) {
        return new TransferStockUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, productRepository);
    }

    @Bean
    public GetWarehouseStockUseCase getWarehouseStockUseCase(WarehouseRepository warehouseRepository,
                                                              WarehouseStockRepository warehouseStockRepository,
                                                              ProductRepository productRepository) {
        return new GetWarehouseStockUseCase(warehouseRepository, warehouseStockRepository, productRepository);
    }

    @Bean
    public GetStockMovementsUseCase getStockMovementsUseCase(WarehouseRepository warehouseRepository,
                                                              StockMovementRepository stockMovementRepository,
                                                              ProductRepository productRepository) {
        return new GetStockMovementsUseCase(warehouseRepository, stockMovementRepository, productRepository);
    }
}
