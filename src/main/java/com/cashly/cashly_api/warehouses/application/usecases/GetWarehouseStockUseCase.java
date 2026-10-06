package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.application.dto.response.WarehouseStockResponse;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.UUID;

public class GetWarehouseStockUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final ProductRepository productRepository;

    public GetWarehouseStockUseCase(WarehouseRepository warehouseRepository,
                                     WarehouseStockRepository warehouseStockRepository,
                                     ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.productRepository = productRepository;
    }

    public PageResponse<WarehouseStockResponse> execute(UUID warehouseId, int page, int size) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        PageResponse<WarehouseStock> result =
                warehouseStockRepository.findByWarehouseIdPaged(warehouseId, page, size);

        return new PageResponse<>(
                result.content().stream().map(stock -> {
                    var product = productRepository.findById(stock.getProductId());
                    String code = product.map(p -> p.getCode()).orElse("UNKNOWN");
                    String name = product.map(p -> p.getName()).orElse("Unknown");
                    boolean composite = product.map(p -> p.isComposite()).orElse(false);
                    return WarehouseStockResponse.from(stock, code, name, composite);
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
