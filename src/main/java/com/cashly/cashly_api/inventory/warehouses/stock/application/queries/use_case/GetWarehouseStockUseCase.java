package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.WarehouseStockResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;

import java.util.UUID;

public class GetWarehouseStockUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockCatalogGateway catalog;

    public GetWarehouseStockUseCase(WarehouseRepository warehouseRepository,
            WarehouseStockRepository warehouseStockRepository,
            StockCatalogGateway catalog) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.catalog = catalog;
    }

    public PageResponse<WarehouseStockResponse> execute(UUID warehouseId,
            int page, int size) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        PageResponse<WarehouseStock> result = warehouseStockRepository
                .findByWarehouseIdPaged(warehouseId, page, size);

        return new PageResponse<>(result.content().stream().map(stock -> {
            var product = catalog.findProduct(stock.getProductId());
            String code = product.map(p -> p.code()).orElse("UNKNOWN");
            String name = product.map(p -> p.name()).orElse("Unknown");
            boolean composite = product.map(p -> p.composite()).orElse(false);
            return WarehouseStockResponse.from(stock, code, name, composite);
        }).toList(), result.totalElements(), result.totalPages(), result.size(),
                result.number(), result.first(), result.last(), result.empty());
    }
}
