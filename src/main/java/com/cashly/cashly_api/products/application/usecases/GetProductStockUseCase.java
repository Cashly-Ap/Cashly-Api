package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.products.application.dto.response.ProductStockByWarehouseResponse;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;

import java.util.List;
import com.cashly.cashly_api.products.application.contracts.ProductWarehouseStockGateway;
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

        return stocks.findByProductId(productId).stream().map(ProductStockByWarehouseResponse::from).toList();
    }
}
