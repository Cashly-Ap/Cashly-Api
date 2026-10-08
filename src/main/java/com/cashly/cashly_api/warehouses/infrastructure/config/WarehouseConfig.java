package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.application.contracts.StockCatalogGateway;


import com.cashly.cashly_api.warehouses.application.usecases.*;
import com.cashly.cashly_api.warehouses.application.api.InvoiceInventoryApi;
import com.cashly.cashly_api.warehouses.application.services.InvoiceInventoryService;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseConfig {
    @Bean
    public InvoiceInventoryApi invoiceInventoryApi(
            WarehouseStockRepository stocks, StockMovementRepository movements) {
        return new InvoiceInventoryService(stocks, movements);
    }

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
                                                StockCatalogGateway catalog) {
        return new StockEntryUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, catalog);
    }

    @Bean
    public StockExitUseCase stockExitUseCase(WarehouseRepository warehouseRepository,
                                              WarehouseStockRepository warehouseStockRepository,
                                              StockMovementRepository stockMovementRepository,
                                              StockCatalogGateway catalog) {
        return new StockExitUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, catalog);
    }

    @Bean
    public StockAdjustmentUseCase stockAdjustmentUseCase(WarehouseRepository warehouseRepository,
                                                          WarehouseStockRepository warehouseStockRepository,
                                                          StockMovementRepository stockMovementRepository,
                                                          StockCatalogGateway catalog) {
        return new StockAdjustmentUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, catalog);
    }

    @Bean
    public TransferStockUseCase transferStockUseCase(WarehouseRepository warehouseRepository,
                                                      WarehouseStockRepository warehouseStockRepository,
                                                      StockMovementRepository stockMovementRepository,
                                                      StockCatalogGateway catalog) {
        return new TransferStockUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, catalog);
    }

    @Bean
    public GetWarehouseStockUseCase getWarehouseStockUseCase(WarehouseRepository warehouseRepository,
                                                              WarehouseStockRepository warehouseStockRepository,
                                                              StockCatalogGateway catalog) {
        return new GetWarehouseStockUseCase(warehouseRepository, warehouseStockRepository, catalog);
    }

    @Bean
    public GetStockMovementsUseCase getStockMovementsUseCase(WarehouseRepository warehouseRepository,
                                                               StockMovementRepository stockMovementRepository,
                                                               StockCatalogGateway catalog) {
        return new GetStockMovementsUseCase(warehouseRepository, stockMovementRepository, catalog);
    }


}
