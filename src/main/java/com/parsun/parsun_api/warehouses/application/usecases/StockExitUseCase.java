package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.application.dto.request.StockExitRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.StockMovementResponse;
import com.parsun.parsun_api.warehouses.application.exceptions.InsufficientStockException;
import com.parsun.parsun_api.warehouses.application.exceptions.ProductNotInWarehouseException;
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
    public StockMovementResponse execute(UUID warehouseId, StockExitRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        WarehouseStock stock = warehouseStockRepository
                .findByWarehouseIdAndProductId(warehouseId, product.getId())
                .orElseThrow(() -> new ProductNotInWarehouseException(product.getId(), warehouseId));

        if (stock.getQuantity() < request.quantity()) {
            throw new InsufficientStockException(product.getId(), warehouseId,
                    stock.getQuantity(), request.quantity());
        }

        int prevQty = stock.getQuantity();
        stock.removeStock(request.quantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.create(
                warehouseId, product.getId(), MovementType.EXIT,
                request.quantity(), prevQty, savedStock.getQuantity(),
                null, null, request.notes()
        );
        StockMovement saved = stockMovementRepository.save(movement);
        return StockMovementResponse.from(saved, product.getCode(), product.getName());
    }
}
