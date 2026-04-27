package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.application.dto.response.StockMovementResponse;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;

import java.util.UUID;

public class GetStockMovementsUseCase {

    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public GetStockMovementsUseCase(WarehouseRepository warehouseRepository,
                                     StockMovementRepository stockMovementRepository,
                                     ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    public PageResponse<StockMovementResponse> execute(UUID warehouseId, UUID productId,
                                                        int page, int size) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        PageResponse<StockMovement> result = (productId != null)
                ? stockMovementRepository.findByWarehouseIdAndProductId(warehouseId, productId, page, size)
                : stockMovementRepository.findByWarehouseId(warehouseId, page, size);

        return new PageResponse<>(
                result.content().stream().map(m -> {
                    var product = productRepository.findById(m.getProductId());
                    String code = product.map(p -> p.getCode()).orElse("UNKNOWN");
                    String name = product.map(p -> p.getName()).orElse("Unknown");
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
}
