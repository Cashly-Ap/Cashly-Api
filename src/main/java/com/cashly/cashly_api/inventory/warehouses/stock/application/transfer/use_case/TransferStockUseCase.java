package com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.request.TransferStockRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockCatalogGateway.CatalogProduct;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class TransferStockUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;

    private final ActivityNotificationRecorder activityNotifications;

    public TransferStockUseCase(WarehouseRepository warehouseRepository,
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
    @Audited(module = "inventory", entityType = "WAREHOUSE", action = "TRANSFER")
    public List<StockMovementResponse> execute(UUID sourceWarehouseId,
            TransferStockRequest request) {
        Warehouse source = warehouseRepository.findById(sourceWarehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        sourceWarehouseId));
        if (!source.isActive())
            throw new WarehouseNotActiveException(sourceWarehouseId);

        Warehouse destination = warehouseRepository
                .findById(request.destinationWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(
                        request.destinationWarehouseId()));
        if (!destination.isActive())
            throw new WarehouseNotActiveException(
                    request.destinationWarehouseId());

        CatalogProduct product = catalog.getProduct(request.productId());

        if (!warehouseStockRepository.existsByWarehouseIdAndProductId(
                sourceWarehouseId, product.id())) {
            throw new ProductNotInWarehouseException(product.id(),
                    sourceWarehouseId);
        }

        Map<UUID, WarehouseStock> lockedStocks = new HashMap<>();
        Stream.of(sourceWarehouseId, request.destinationWarehouseId())
                .distinct().sorted(Comparator.naturalOrder())
                .forEach(warehouseId -> lockedStocks.put(warehouseId,
                        warehouseStockRepository
                                .findOrCreateByWarehouseIdAndProductIdForUpdate(
                                        warehouseId, product.id())));
        WarehouseStock sourceStock = lockedStocks.get(sourceWarehouseId);

        if (sourceStock.getQuantity() < request.quantity()) {
            throw new InsufficientStockException(product.id(),
                    sourceWarehouseId, sourceStock.getQuantity(),
                    request.quantity());
        }

        String batchId = UUID.randomUUID().toString();

        int sourcePrev = sourceStock.getQuantity();
        sourceStock.removeStock(request.quantity());
        WarehouseStock savedSource = warehouseStockRepository.save(sourceStock);

        StockMovement outMovement = StockMovement.createWithReference(
                sourceWarehouseId, product.id(), MovementType.TRANSFER_OUT,
                request.quantity(), sourcePrev, savedSource.getQuantity(), null,
                batchId, ReferenceType.TRANSFER, null, batchId,
                request.notes());
        StockMovement savedOut = stockMovementRepository.save(outMovement);

        WarehouseStock destStock = lockedStocks
                .get(request.destinationWarehouseId());

        int destPrev = destStock.getQuantity();
        destStock.addStock(request.quantity());
        WarehouseStock savedDest = warehouseStockRepository.save(destStock);

        StockMovement inMovement = StockMovement.createWithReference(
                request.destinationWarehouseId(), product.id(),
                MovementType.TRANSFER_IN, request.quantity(), destPrev,
                savedDest.getQuantity(), savedOut.getId(), batchId,
                ReferenceType.TRANSFER, null, batchId, request.notes());
        StockMovement savedIn = stockMovementRepository.save(inMovement);

        activityNotifications.record(ActivityEvent.of("INVENTORY_WAREHOUSES",
                "RECORD_EXIT", "WAREHOUSE", sourceWarehouseId,
                source.getName(), null));
        activityNotifications.record(ActivityEvent.of("INVENTORY_WAREHOUSES",
                "RECORD_ENTRY", "WAREHOUSE",
                request.destinationWarehouseId(), destination.getName(), null));
        return List.of(
                StockMovementResponse.from(savedOut, product.code(),
                        product.name()),
                StockMovementResponse.from(savedIn, product.code(),
                        product.name()));
    }
}
