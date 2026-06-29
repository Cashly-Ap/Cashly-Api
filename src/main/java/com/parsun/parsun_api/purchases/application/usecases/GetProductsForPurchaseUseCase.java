package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseProductResponse;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class GetProductsForPurchaseUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public GetProductsForPurchaseUseCase(ProductRepository productRepository,
                                          CategoryRepository categoryRepository,
                                          WarehouseStockRepository warehouseStockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseProductResponse> execute(String search, UUID categoryId,
                                                          int page, int size) {
        PageResponse<Product> productsPage = productRepository.findAllWithFilters(
                search, categoryId, true, false, page, size, "name", "asc");

        List<PurchaseProductResponse> content = productsPage.content().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(content, productsPage.totalElements(), productsPage.totalPages(),
                productsPage.size(), productsPage.number(), productsPage.first(),
                productsPage.last(), productsPage.empty());
    }

    private PurchaseProductResponse toResponse(Product product) {
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(c -> c.getName())
                .orElse("");

        int totalStock = warehouseStockRepository.findByProductId(product.getId()).stream()
                .mapToInt(s -> s.getQuantity())
                .sum();

        return new PurchaseProductResponse(
                product.getId().toString(),
                product.getCode(),
                product.getName(),
                product.getCategoryId().toString(),
                categoryName,
                product.isActive(),
                totalStock
        );
    }
}
