package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.warehouses.application.dto.request.TransferStockRequest;
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

import java.util.List;
import java.util.UUID;

public class TransferStockUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public TransferStockUseCase(WarehouseRepository warehouseRepository,
                                 WarehouseStockRepository warehouseStockRepository,
                                 StockMovementRepository stockMovementRepository,
                                 ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public List<StockMovementResponse> execute(UUID sourceWarehouseId, TransferStockRequest request) {
        Warehouse source = warehouseRepository.findById(sourceWarehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(sourceWarehouseId));
        if (!source.isActive()) throw new WarehouseNotActiveException(sourceWarehouseId);

        Warehouse destination = warehouseRepository.findById(request.destinationWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.destinationWarehouseId()));
        if (!destination.isActive()) throw new WarehouseNotActiveException(request.destinationWarehouseId());

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        
        WarehouseStock sourceStock = warehouseStockRepository
                .findByWarehouseIdAndProductId(sourceWarehouseId, product.getId())
                .orElseThrow(() -> new ProductNotInWarehouseException(product.getId(), sourceWarehouseId));

        if (sourceStock.getQuantity() < request.quantity()) {
            throw new InsufficientStockException(product.getId(), sourceWarehouseId,
                    sourceStock.getQuantity(), request.quantity());
        }

        String batchId = UUID.randomUUID().toString();

        int sourcePrev = sourceStock.getQuantity();
        sourceStock.removeStock(request.quantity());
        WarehouseStock savedSource = warehouseStockRepository.save(sourceStock);

        StockMovement outMovement = StockMovement.createWithReference(
                sourceWarehouseId, product.getId(), MovementType.TRANSFER_OUT,
                request.quantity(), sourcePrev, savedSource.getQuantity(),
                null, batchId, ReferenceType.TRANSFER, null, batchId, request.notes()
        );
        StockMovement savedOut = stockMovementRepository.save(outMovement);

        
        WarehouseStock destStock = warehouseStockRepository
                .findByWarehouseIdAndProductId(request.destinationWarehouseId(), product.getId())
                .orElseGet(() -> WarehouseStock.create(request.destinationWarehouseId(), product.getId()));

        int destPrev = destStock.getQuantity();
        destStock.addStock(request.quantity());
        WarehouseStock savedDest = warehouseStockRepository.save(destStock);

        StockMovement inMovement = StockMovement.createWithReference(
                request.destinationWarehouseId(), product.getId(), MovementType.TRANSFER_IN,
                request.quantity(), destPrev, savedDest.getQuantity(),
                savedOut.getId(), batchId, ReferenceType.TRANSFER, null, batchId, request.notes()
        );
        StockMovement savedIn = stockMovementRepository.save(inMovement);

        return List.of(
                StockMovementResponse.from(savedOut, product.getCode(), product.getName()),
                StockMovementResponse.from(savedIn, product.getCode(), product.getName())
        );
    }
}
