package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
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

public class StockExitUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockExitUseCase(WarehouseRepository warehouseRepository,
                             WarehouseStockRepository warehouseStockRepository,
                             StockMovementRepository stockMovementRepository,
                             ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public List<StockMovementResponse> execute(UUID warehouseId, StockExitRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        for (StockBatchItem item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(item.productId()));

            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(warehouseId, product.getId())
                    .orElseThrow(() -> new ProductNotInWarehouseException(product.getId(), warehouseId));

            if (stock.getQuantity() < item.quantity()) {
                throw new InsufficientStockException(product.getId(), warehouseId,
                        stock.getQuantity(), item.quantity());
            }
        }

        List<StockMovementResponse> movements = new ArrayList<>();

        for (StockBatchItem item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(item.productId()));

            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(warehouseId, product.getId())
                    .orElseThrow(() -> new ProductNotInWarehouseException(product.getId(), warehouseId));

            int prevQty = stock.getQuantity();
            stock.removeStock(item.quantity());
            WarehouseStock savedStock = warehouseStockRepository.save(stock);

            StockMovement movement = StockMovement.createWithReference(
                    warehouseId, product.getId(), MovementType.EXIT,
                    item.quantity(), prevQty, savedStock.getQuantity(),
                    null, null, ReferenceType.MANUAL, null, null, item.notes()
            );
            StockMovement saved = stockMovementRepository.save(movement);
            movements.add(StockMovementResponse.from(saved, product.getCode(), product.getName()));
        }

        return movements;
    }
}
