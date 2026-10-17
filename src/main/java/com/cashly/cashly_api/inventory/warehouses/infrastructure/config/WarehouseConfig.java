package com.cashly.cashly_api.inventory.warehouses.infrastructure.config;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockCatalogGateway;


import com.cashly.cashly_api.inventory.warehouses.management.application.create.use_case.CreateWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.delete.use_case.DeleteWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case.GetStockMovementsUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case.GetWarehouseStockUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.queries.use_case.GetWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.queries.use_case.ListWarehousesUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.adjustment.use_case.StockAdjustmentUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.entry.use_case.StockEntryUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exit.use_case.StockExitUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.use_case.TransferStockUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.update.use_case.UpdateWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.InvoiceInventoryApi;
import com.cashly.cashly_api.inventory.warehouses.stock.application.invoices.services.InvoiceInventoryService;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
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
