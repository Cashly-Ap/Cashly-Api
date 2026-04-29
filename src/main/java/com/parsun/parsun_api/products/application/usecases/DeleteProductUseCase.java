package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public DeleteProductUseCase(ProductRepository productRepository,
                                 StockMovementRepository stockMovementRepository,
                                 WarehouseStockRepository warehouseStockRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new ProductNotFoundException(id);
        }
        stockMovementRepository.deleteByProductId(id);
        warehouseStockRepository.deleteByProductId(id);
        productRepository.deleteComponentReferencesByProductId(id);
        productRepository.delete(id);
    }
}
