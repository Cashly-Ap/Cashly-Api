package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.warehouses.application.dto.request.StockEntryRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.StockMovementResponse;
import com.parsun.parsun_api.warehouses.application.exceptions.InsufficientStockException;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotActiveException;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles stock entry for a warehouse.
 *
 * IMPORTANT: If the product is composite (composite=true), the entry does NOT add stock
 * for the parent product. Instead, it deducts the required components from the warehouse
 * stock and creates COMPONENT_EXIT movements for each. If any component has insufficient
 * stock, InsufficientStockException is thrown.
 */
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

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        List<StockMovementResponse> movements = new ArrayList<>();

        if (product.isComposite()) {
            // Composite: deduct each component from warehouse stock
            for (var component : product.getComponents()) {
                int needed = component.getQuantity() * request.quantity();
                UUID componentId = component.getComponentProductId();

                WarehouseStock compStock = warehouseStockRepository
                        .findByWarehouseIdAndProductId(warehouseId, componentId)
                        .orElseGet(() -> WarehouseStock.create(warehouseId, componentId));

                if (compStock.getQuantity() < needed) {
                    throw new InsufficientStockException(componentId, warehouseId,
                            compStock.getQuantity(), needed);
                }

                int prevQty = compStock.getQuantity();
                compStock.removeStock(needed);
                WarehouseStock savedStock = warehouseStockRepository.save(compStock);

                Product compProduct = productRepository.findById(componentId)
                        .orElseThrow(() -> new ProductNotFoundException(componentId));

                StockMovement movement = StockMovement.create(
                        warehouseId, componentId, MovementType.COMPONENT_EXIT,
                        needed, prevQty, savedStock.getQuantity(),
                        null, null, request.notes()
                );
                StockMovement saved = stockMovementRepository.save(movement);
                movements.add(StockMovementResponse.from(saved, compProduct.getCode(), compProduct.getName()));
            }
        } else {
            // Simple product: add stock directly
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(warehouseId, product.getId())
                    .orElseGet(() -> WarehouseStock.create(warehouseId, product.getId()));

            int prevQty = stock.getQuantity();
            stock.addStock(request.quantity());
            WarehouseStock savedStock = warehouseStockRepository.save(stock);

            StockMovement movement = StockMovement.create(
                    warehouseId, product.getId(), MovementType.ENTRY,
                    request.quantity(), prevQty, savedStock.getQuantity(),
                    null, null, request.notes()
            );
            StockMovement saved = stockMovementRepository.save(movement);
            movements.add(StockMovementResponse.from(saved, product.getCode(), product.getName()));
        }

        return movements;
    }
}
