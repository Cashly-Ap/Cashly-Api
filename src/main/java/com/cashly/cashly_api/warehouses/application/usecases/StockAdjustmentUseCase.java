package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
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

public class StockAdjustmentUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockAdjustmentUseCase(WarehouseRepository warehouseRepository,
                                   WarehouseStockRepository warehouseStockRepository,
                                   StockMovementRepository stockMovementRepository,
                                   ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public StockMovementResponse execute(UUID warehouseId, StockAdjustmentRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new WarehouseNotActiveException(warehouseId);
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        WarehouseStock stock = warehouseStockRepository
                .findByWarehouseIdAndProductId(warehouseId, product.getId())
                .orElseGet(() -> WarehouseStock.create(warehouseId, product.getId()));

        int prevQty = stock.getQuantity();
        int delta = Math.abs(request.newQuantity() - prevQty);
        stock.adjustStock(request.newQuantity());
        WarehouseStock savedStock = warehouseStockRepository.save(stock);

        StockMovement movement = StockMovement.create(
                warehouseId, product.getId(), MovementType.ADJUSTMENT,
                delta, prevQty, savedStock.getQuantity(),
                null, null, request.notes()
        );
        StockMovement saved = stockMovementRepository.save(movement);
        return StockMovementResponse.from(saved, product.getCode(), product.getName());
    }
}
