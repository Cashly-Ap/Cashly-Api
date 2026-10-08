package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.warehouses.application.dto.request.StockBatchItem;
import com.cashly.cashly_api.warehouses.application.dto.request.StockExitRequest;
import com.cashly.cashly_api.warehouses.application.dto.response.StockMovementResponse;
import com.cashly.cashly_api.warehouses.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.warehouses.application.exceptions.ProductNotInWarehouseException;
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
import java.util.List;
import java.util.UUID;
import com.cashly.cashly_api.warehouses.application.contracts.StockCatalogGateway;
import com.cashly.cashly_api.warehouses.application.contracts.StockCatalogGateway.CatalogProduct;

public class StockExitUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;

    public StockExitUseCase(WarehouseRepository warehouseRepository,
                             WarehouseStockRepository warehouseStockRepository,
                             StockMovementRepository stockMovementRepository,
                             StockCatalogGateway catalog) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.catalog = catalog;
    }

    @Transactional
    public List<StockMovementResponse> execute(UUID warehouseId, StockExitRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        for (StockBatchItem item : request.items()) {
            CatalogProduct product = catalog.getProduct(item.productId());

            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductIdForUpdate(warehouseId, product.id())
                    .orElseThrow(() -> new ProductNotInWarehouseException(product.id(), warehouseId));

            if (stock.getQuantity() < item.quantity()) {
                throw new InsufficientStockException(product.id(), warehouseId,
                        stock.getQuantity(), item.quantity());
            }
        }

        List<StockMovementResponse> movements = new ArrayList<>();

        for (StockBatchItem item : request.items()) {
            CatalogProduct product = catalog.getProduct(item.productId());

            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductIdForUpdate(warehouseId, product.id())
                    .orElseThrow(() -> new ProductNotInWarehouseException(product.id(), warehouseId));

            int prevQty = stock.getQuantity();
            stock.removeStock(item.quantity());
            WarehouseStock savedStock = warehouseStockRepository.save(stock);

            StockMovement movement = StockMovement.createWithReference(
                    warehouseId, product.id(), MovementType.EXIT,
                    item.quantity(), prevQty, savedStock.getQuantity(),
                    null, null, ReferenceType.MANUAL, null, null, item.notes()
            );
            StockMovement saved = stockMovementRepository.save(movement);
            movements.add(StockMovementResponse.from(saved, product.code(), product.name()));
        }

        return movements;
    }
}
