package com.cashly.cashly_api.catalog.products.application.stock.use_case;

import com.cashly.cashly_api.catalog.products.application.stock.response.ProductStockByWarehouseResponse;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;

import java.util.List;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductWarehouseStockGateway;
import java.util.UUID;

public class GetProductStockUseCase {

    private final ProductRepository productRepository;
    private final ProductWarehouseStockGateway stocks;

    public GetProductStockUseCase(ProductRepository productRepository,
            ProductWarehouseStockGateway stocks) {
        this.productRepository = productRepository;
        this.stocks = stocks;
    }

    public List<ProductStockByWarehouseResponse> execute(UUID productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return stocks.findByProductId(productId).stream()
                .map(ProductStockByWarehouseResponse::from).toList();
    }
}
