package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.entities.ProductComponent;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.warehouses.application.dto.request.StockBatchItem;
import com.cashly.cashly_api.warehouses.application.dto.request.StockEntryRequest;
import com.cashly.cashly_api.warehouses.application.dto.response.StockMovementResponse;
import com.cashly.cashly_api.warehouses.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.ReferenceType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StockEntryUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockEntryUseCase(WarehouseRepository warehouseRepository,
                              WarehouseStockRepository warehouseStockRepository,
                              StockMovementRepository stockMovementRepository,
                              ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public List<StockMovementResponse> execute(UUID warehouseId, StockEntryRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        Map<UUID, Product> productCache = new HashMap<>();
        List<StockMovementResponse> movements = new ArrayList<>();

        for (StockBatchItem item : request.items()) {
            Product product = productCache.computeIfAbsent(item.productId(), id ->
                    productRepository.findById(id)
                            .orElseThrow(() -> new ProductNotFoundException(id))
            );

            if (product.isComposite()) {
                processCompositeProduct(warehouseId, item, product, productCache, movements);
            } else {
                processSimpleProduct(warehouseId, item, product, movements);
            }
        }

        return movements;
    }

    private void processSimpleProduct(UUID warehouseId, StockBatchItem item, Product product,
                                       List<StockMovementResponse> movements) {
        WarehouseStock stock = warehouseStockRepository
                .findByWarehouseIdAndProductId(warehouseId, product.getId())
                .orElseGet(() -> WarehouseStock.create(warehouseId, product.getId()));

        int prevQty = stock.getQuantity();
        stock.addStock(item.quantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.createWithReference(
                warehouseId, product.getId(), MovementType.ENTRY,
                item.quantity(), prevQty, savedStock.getQuantity(),
                null, null, ReferenceType.MANUAL, null, null, item.notes()
        );
        StockMovement saved = stockMovementRepository.save(movement);
        movements.add(StockMovementResponse.from(saved, product.getCode(), product.getName()));
    }

    private void processCompositeProduct(UUID warehouseId, StockBatchItem item, Product product,
                                          Map<UUID, Product> productCache,
                                          List<StockMovementResponse> movements) {
        for (ProductComponent component : product.getComponents()) {
            int required = component.getQuantity() * item.quantity();
            WarehouseStock componentStock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(warehouseId, component.getComponentProductId())
                    .orElseThrow(() -> new InsufficientStockException(
                            component.getComponentProductId(), warehouseId, 0, required));

            if (componentStock.getQuantity() < required) {
                throw new InsufficientStockException(
                        component.getComponentProductId(), warehouseId,
                        componentStock.getQuantity(), required);
            }

            int prevComponentQty = componentStock.getQuantity();
            componentStock.removeStock(required);
            WarehouseStock savedComponent = warehouseStockRepository.save(componentStock);

            Product componentProduct = productCache.computeIfAbsent(component.getComponentProductId(), id ->
                    productRepository.findById(id)
                            .orElseThrow(() -> new ProductNotFoundException(id))
            );

            StockMovement componentMovement = StockMovement.createWithReference(
                    warehouseId, component.getComponentProductId(), MovementType.COMPONENT_EXIT,
                    required, prevComponentQty, savedComponent.getQuantity(),
                    null, null, ReferenceType.MANUAL, null, null,
                    "Component consumed for: " + product.getName()
            );
            StockMovement savedComponentMovement = stockMovementRepository.save(componentMovement);
            movements.add(StockMovementResponse.from(savedComponentMovement,
                    componentProduct.getCode(), componentProduct.getName()));
        }

        WarehouseStock stock = warehouseStockRepository
                .findByWarehouseIdAndProductId(warehouseId, product.getId())
                .orElseGet(() -> WarehouseStock.create(warehouseId, product.getId()));

        int prevQty = stock.getQuantity();
        stock.addStock(item.quantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.createWithReference(
                warehouseId, product.getId(), MovementType.ENTRY,
                item.quantity(), prevQty, savedStock.getQuantity(),
                null, null, ReferenceType.MANUAL, null, null, item.notes()
        );
        StockMovement saved = stockMovementRepository.save(movement);
        movements.add(StockMovementResponse.from(saved, product.getCode(), product.getName()));
    }
}
