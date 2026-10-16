package com.cashly.cashly_api.warehouses.stock.application.entry;

import com.cashly.cashly_api.warehouses.stock.application.dto.StockBatchItem;
import com.cashly.cashly_api.warehouses.stock.application.entry.StockEntryRequest;
import com.cashly.cashly_api.warehouses.stock.application.movements.StockMovementResponse;
import com.cashly.cashly_api.warehouses.stock.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.ReferenceType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway.CatalogProduct;

public class StockEntryUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;

    public StockEntryUseCase(WarehouseRepository warehouseRepository,
                              WarehouseStockRepository warehouseStockRepository,
                              StockMovementRepository stockMovementRepository,
                              StockCatalogGateway catalog) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.catalog = catalog;
    }

    @Transactional
    public List<StockMovementResponse> execute(UUID warehouseId, StockEntryRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        Map<UUID, CatalogProduct> productCache = new HashMap<>();
        List<StockMovementResponse> movements = new ArrayList<>();

        for (StockBatchItem item : request.items()) {
            CatalogProduct product = productCache.computeIfAbsent(item.productId(), id ->
                    catalog.getProduct(id)
            );

            if (product.composite()) {
                processCompositeProduct(warehouseId, item, product, productCache, movements);
            } else {
                processSimpleProduct(warehouseId, item, product, movements);
            }
        }

        return movements;
    }

    private void processSimpleProduct(UUID warehouseId, StockBatchItem item, CatalogProduct product,
                                       List<StockMovementResponse> movements) {
        WarehouseStock stock = warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, product.id());

        int prevQty = stock.getQuantity();
        stock.addStock(item.quantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.createWithReference(
                warehouseId, product.id(), MovementType.ENTRY,
                item.quantity(), prevQty, savedStock.getQuantity(),
                null, null, ReferenceType.MANUAL, null, null, item.notes()
        );
        StockMovement saved = stockMovementRepository.save(movement);
        movements.add(StockMovementResponse.from(saved, product.code(), product.name()));
    }

    private void processCompositeProduct(UUID warehouseId, StockBatchItem item, CatalogProduct product,
                                          Map<UUID, CatalogProduct> productCache,
                                          List<StockMovementResponse> movements) {
        for (StockCatalogGateway.Component component : product.components()) {
            int required = component.quantity() * item.quantity();
            WarehouseStock componentStock = warehouseStockRepository
                    .findByWarehouseIdAndProductIdForUpdate(warehouseId, component.productId())
                    .orElseThrow(() -> new InsufficientStockException(
                            component.productId(), warehouseId, 0, required));

            if (componentStock.getQuantity() < required) {
                throw new InsufficientStockException(
                        component.productId(), warehouseId,
                        componentStock.getQuantity(), required);
            }

            int prevComponentQty = componentStock.getQuantity();
            componentStock.removeStock(required);
            WarehouseStock savedComponent = warehouseStockRepository.save(componentStock);

            CatalogProduct componentProduct = productCache.computeIfAbsent(component.productId(), id ->
                    catalog.getProduct(id)
            );

            StockMovement componentMovement = StockMovement.createWithReference(
                    warehouseId, component.productId(), MovementType.COMPONENT_EXIT,
                    required, prevComponentQty, savedComponent.getQuantity(),
                    null, null, ReferenceType.MANUAL, null, null,
                    "Component consumed for: " + product.name()
            );
            StockMovement savedComponentMovement = stockMovementRepository.save(componentMovement);
            movements.add(StockMovementResponse.from(savedComponentMovement,
                    componentProduct.code(), componentProduct.name()));
        }

        WarehouseStock stock = warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, product.id());

        int prevQty = stock.getQuantity();
        stock.addStock(item.quantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.createWithReference(
                warehouseId, product.id(), MovementType.ENTRY,
                item.quantity(), prevQty, savedStock.getQuantity(),
                null, null, ReferenceType.MANUAL, null, null, item.notes()
        );
        StockMovement saved = stockMovementRepository.save(movement);
        movements.add(StockMovementResponse.from(saved, product.code(), product.name()));
    }
}
