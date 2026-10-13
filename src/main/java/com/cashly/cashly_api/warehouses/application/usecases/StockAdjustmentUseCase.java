package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.warehouses.application.dto.request.StockAdjustmentRequest;
import com.cashly.cashly_api.warehouses.application.dto.response.StockMovementResponse;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.warehouses.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.warehouses.application.ports.out.StockCatalogGateway.CatalogProduct;

public class StockAdjustmentUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;

    public StockAdjustmentUseCase(WarehouseRepository warehouseRepository,
                                   WarehouseStockRepository warehouseStockRepository,
                                   StockMovementRepository stockMovementRepository,
                                   StockCatalogGateway catalog) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.catalog = catalog;
    }

    @Transactional
    public StockMovementResponse execute(UUID warehouseId, StockAdjustmentRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        CatalogProduct product = catalog.getProduct(request.productId());

        WarehouseStock stock = warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, product.id());

        int prevQty = stock.getQuantity();
        int delta = Math.abs(request.newQuantity() - prevQty);
        stock.adjustStock(request.newQuantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.create(
                warehouseId, product.id(), MovementType.ADJUSTMENT,
                delta, prevQty, savedStock.getQuantity(),
                null, null, request.notes()
        );
        StockMovement saved = stockMovementRepository.save(movement);
        return StockMovementResponse.from(saved, product.code(), product.name());
    }
}
