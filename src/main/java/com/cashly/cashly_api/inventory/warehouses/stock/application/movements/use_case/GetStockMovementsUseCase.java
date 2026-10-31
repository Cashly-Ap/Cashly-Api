package com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case;

import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockMovementQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetStockMovementsUseCase {

    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;
    private final StockMovementQueryGateway movementQueryGateway;

    public GetStockMovementsUseCase(WarehouseRepository warehouseRepository,
            StockMovementRepository stockMovementRepository,
            StockCatalogGateway catalog,
            StockMovementQueryGateway movementQueryGateway) {
        this.warehouseRepository = warehouseRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.catalog = catalog;
        this.movementQueryGateway = movementQueryGateway;
    }

    public PageResponse<StockMovementResponse> execute(UUID warehouseId,
            UUID productId, MovementType movementType, int page, int size) {
        var warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        PageResponse<StockMovement> result = findMovements(warehouseId,
                productId, movementType, page, size);

        return new PageResponse<>(result.content().stream().map(m -> {
            var product = catalog.findProduct(m.getProductId());
            String code = product.map(p -> p.code()).orElse("UNKNOWN");
            String name = product.map(p -> p.name()).orElse("Unknown");
            return StockMovementResponse.from(m, warehouse.getName(), code,
                    name);
        }).toList(), result.totalElements(), result.totalPages(), result.size(),
                result.number(), result.first(), result.last(), result.empty());
    }

    @Transactional(readOnly = true)
    public StockMovementsResponse execute(StockMovementFilter filter) {
        if (filter.warehouseId() != null) {
            warehouseRepository.findById(filter.warehouseId())
                    .orElseThrow(() -> new WarehouseNotFoundException(
                            filter.warehouseId()));
        }
        return movementQueryGateway.search(filter);
    }

    private PageResponse<StockMovement> findMovements(UUID warehouseId,
            UUID productId, MovementType movementType, int page, int size) {
        if (productId != null && movementType != null) {
            return stockMovementRepository
                    .findByWarehouseIdAndProductIdAndMovementType(warehouseId,
                            productId, movementType, page, size);
        }
        if (productId != null) {
            return stockMovementRepository.findByWarehouseIdAndProductId(
                    warehouseId, productId, page, size);
        }
        if (movementType != null) {
            return stockMovementRepository.findByWarehouseIdAndMovementType(
                    warehouseId, movementType, page, size);
        }
        return stockMovementRepository.findByWarehouseId(warehouseId, page,
                size);
    }
}
