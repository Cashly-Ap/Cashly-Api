package com.cashly.cashly_api.warehouses.stock.application.transfer;

import com.cashly.cashly_api.warehouses.stock.application.transfer.TransferStockRequest;
import com.cashly.cashly_api.warehouses.stock.application.movements.StockMovementResponse;
import com.cashly.cashly_api.warehouses.stock.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
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

import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway.CatalogProduct;

public class TransferStockUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;

    public TransferStockUseCase(WarehouseRepository warehouseRepository,
                                 WarehouseStockRepository warehouseStockRepository,
                                 StockMovementRepository stockMovementRepository,
                                 StockCatalogGateway catalog) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.catalog = catalog;
    }

    @Transactional
    public List<StockMovementResponse> execute(UUID sourceWarehouseId, TransferStockRequest request) {
        Warehouse source = warehouseRepository.findById(sourceWarehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(sourceWarehouseId));
        if (!source.isActive()) throw new WarehouseNotActiveException(sourceWarehouseId);

        Warehouse destination = warehouseRepository.findById(request.destinationWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.destinationWarehouseId()));
        if (!destination.isActive()) throw new WarehouseNotActiveException(request.destinationWarehouseId());

        CatalogProduct product = catalog.getProduct(request.productId());

        if (!warehouseStockRepository.existsByWarehouseIdAndProductId(sourceWarehouseId, product.id())) {
            throw new ProductNotInWarehouseException(product.id(), sourceWarehouseId);
        }

        Map<UUID, WarehouseStock> lockedStocks = new HashMap<>();
        Stream.of(sourceWarehouseId, request.destinationWarehouseId())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .forEach(warehouseId -> lockedStocks.put(warehouseId,
                        warehouseStockRepository.findOrCreateByWarehouseIdAndProductIdForUpdate(
                                warehouseId, product.id())));
        WarehouseStock sourceStock = lockedStocks.get(sourceWarehouseId);

        if (sourceStock.getQuantity() < request.quantity()) {
            throw new InsufficientStockException(product.id(), sourceWarehouseId,
                    sourceStock.getQuantity(), request.quantity());
        }

        String batchId = UUID.randomUUID().toString();

        int sourcePrev = sourceStock.getQuantity();
        sourceStock.removeStock(request.quantity());
        WarehouseStock savedSource = warehouseStockRepository.save(sourceStock);

        StockMovement outMovement = StockMovement.createWithReference(
                sourceWarehouseId, product.id(), MovementType.TRANSFER_OUT,
                request.quantity(), sourcePrev, savedSource.getQuantity(),
                null, batchId, ReferenceType.TRANSFER, null, batchId, request.notes()
        );
        StockMovement savedOut = stockMovementRepository.save(outMovement);

        WarehouseStock destStock = lockedStocks.get(request.destinationWarehouseId());

        int destPrev = destStock.getQuantity();
        destStock.addStock(request.quantity());
        WarehouseStock savedDest = warehouseStockRepository.save(destStock);

        StockMovement inMovement = StockMovement.createWithReference(
                request.destinationWarehouseId(), product.id(), MovementType.TRANSFER_IN,
                request.quantity(), destPrev, savedDest.getQuantity(),
                savedOut.getId(), batchId, ReferenceType.TRANSFER, null, batchId, request.notes()
        );
        StockMovement savedIn = stockMovementRepository.save(inMovement);

        return List.of(
                StockMovementResponse.from(savedOut, product.code(), product.name()),
                StockMovementResponse.from(savedIn, product.code(), product.name())
        );
    }
}
