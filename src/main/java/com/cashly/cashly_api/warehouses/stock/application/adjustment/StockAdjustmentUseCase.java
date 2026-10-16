package com.cashly.cashly_api.warehouses.stock.application.adjustment;

import com.cashly.cashly_api.warehouses.stock.application.adjustment.StockAdjustmentRequest;
import com.cashly.cashly_api.warehouses.stock.application.movements.StockMovementResponse;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway.CatalogProduct;

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
