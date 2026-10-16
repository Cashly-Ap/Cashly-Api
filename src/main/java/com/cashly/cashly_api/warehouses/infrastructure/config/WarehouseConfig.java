package com.cashly.cashly_api.warehouses.infrastructure.config;

import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway;


import com.cashly.cashly_api.warehouses.management.application.create.CreateWarehouseUseCase;
import com.cashly.cashly_api.warehouses.management.application.delete.DeleteWarehouseUseCase;
import com.cashly.cashly_api.warehouses.stock.application.movements.GetStockMovementsUseCase;
import com.cashly.cashly_api.warehouses.stock.application.queries.GetWarehouseStockUseCase;
import com.cashly.cashly_api.warehouses.management.application.queries.GetWarehouseUseCase;
import com.cashly.cashly_api.warehouses.management.application.queries.ListWarehousesUseCase;
import com.cashly.cashly_api.warehouses.stock.application.adjustment.StockAdjustmentUseCase;
import com.cashly.cashly_api.warehouses.stock.application.entry.StockEntryUseCase;
import com.cashly.cashly_api.warehouses.stock.application.exit.StockExitUseCase;
import com.cashly.cashly_api.warehouses.stock.application.transfer.TransferStockUseCase;
import com.cashly.cashly_api.warehouses.management.application.update.UpdateWarehouseUseCase;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.InvoiceInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.invoices.InvoiceInventoryService;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
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
