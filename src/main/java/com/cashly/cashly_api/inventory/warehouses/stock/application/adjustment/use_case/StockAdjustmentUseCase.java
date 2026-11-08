package com.cashly.cashly_api.inventory.warehouses.stock.application.adjustment.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.inventory.warehouses.stock.application.adjustment.request.StockAdjustmentRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockCatalogGateway.CatalogProduct;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class StockAdjustmentUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;

    private final ActivityNotificationRecorder activityNotifications;

    public StockAdjustmentUseCase(WarehouseRepository warehouseRepository,
            WarehouseStockRepository warehouseStockRepository,
            StockMovementRepository stockMovementRepository,
            StockCatalogGateway catalog,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.catalog = catalog;
    }

    @Transactional
    @Audited(module = "inventory", entityType = "WAREHOUSE", action = "ADJUST")
    public StockMovementResponse execute(UUID warehouseId,
            StockAdjustmentRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        CatalogProduct product = catalog.getProduct(request.productId());

        WarehouseStock stock = warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId,
                        product.id());

        int prevQty = stock.getQuantity();
        int delta = Math.abs(request.newQuantity() - prevQty);
        stock.adjustStock(request.newQuantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.create(warehouseId, product.id(),
                MovementType.ADJUSTMENT, delta, prevQty,
                savedStock.getQuantity(), null, null, request.notes());
        StockMovement saved = stockMovementRepository.save(movement);
        activityNotifications.record(ActivityEvent.of("INVENTORY_WAREHOUSES",
                savedStock.getQuantity() >= prevQty
                        ? "RECORD_ENTRY"
                        : "RECORD_EXIT",
                "WAREHOUSE", warehouseId, warehouse.getName(), null));
        return StockMovementResponse.from(saved, product.code(),
                product.name());
    }
}
