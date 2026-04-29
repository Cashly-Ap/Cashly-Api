package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.products.application.dto.response.ProductStockByWarehouseResponse;
import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.List;
import java.util.UUID;

public class GetProductStockUseCase {

    private final ProductRepository productRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;

    public GetProductStockUseCase(ProductRepository productRepository,
                                   WarehouseStockRepository warehouseStockRepository,
                                   WarehouseRepository warehouseRepository) {
        this.productRepository = productRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public List<ProductStockByWarehouseResponse> execute(UUID productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return warehouseStockRepository.findByProductId(productId).stream()
                .map(stock -> {
                    String warehouseName = warehouseRepository.findById(stock.getWarehouseId())
                            .map(w -> w.getName())
                            .orElse("Unknown");
                    return ProductStockByWarehouseResponse.from(stock, warehouseName);
                })
                .toList();
    }
}
