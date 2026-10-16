package com.cashly.cashly_api.warehouses.stock.application.movements.use_case;

import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.repository.WarehouseRepository;

import java.util.UUID;

public class GetStockMovementsUseCase {

    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCatalogGateway catalog;

    public GetStockMovementsUseCase(WarehouseRepository warehouseRepository,
                                     StockMovementRepository stockMovementRepository,
                                     StockCatalogGateway catalog) {
        this.warehouseRepository = warehouseRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.catalog = catalog;
    }

    public PageResponse<StockMovementResponse> execute(UUID warehouseId, UUID productId,
                                                        MovementType movementType,
                                                        int page, int size) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        PageResponse<StockMovement> result = findMovements(
                warehouseId, productId, movementType, page, size);

        return new PageResponse<>(
                result.content().stream().map(m -> {
                    var product = catalog.findProduct(m.getProductId());
                    String code = product.map(p -> p.code()).orElse("UNKNOWN");
                    String name = product.map(p -> p.name()).orElse("Unknown");
                    return StockMovementResponse.from(m, code, name);
                }).toList(),
                result.totalElements(),
                result.totalPages(),
                result.size(),
                result.number(),
                result.first(),
                result.last(),
                result.empty()
        );
    }

    private PageResponse<StockMovement> findMovements(UUID warehouseId, UUID productId,
                                                       MovementType movementType,
                                                       int page, int size) {
        if (productId != null && movementType != null) {
            return stockMovementRepository.findByWarehouseIdAndProductIdAndMovementType(
                    warehouseId, productId, movementType, page, size);
        }
        if (productId != null) {
            return stockMovementRepository.findByWarehouseIdAndProductId(
                    warehouseId, productId, page, size);
        }
        if (movementType != null) {
            return stockMovementRepository.findByWarehouseIdAndMovementType(
                    warehouseId, movementType, page, size);
        }
        return stockMovementRepository.findByWarehouseId(warehouseId, page, size);
    }
}
