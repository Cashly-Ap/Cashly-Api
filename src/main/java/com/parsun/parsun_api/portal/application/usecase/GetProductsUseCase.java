package com.parsun.parsun_api.portal.application.usecase;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.portal.application.dto.response.PortalProductResponse;
import com.parsun.parsun_api.portal.application.mapper.PortalProductMapper;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.UUID;

public class GetProductsUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public GetProductsUseCase(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               WarehouseStockRepository warehouseStockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    public PageResponse<PortalProductResponse> execute(String search, String categoryId, int page, int size) {
        UUID categoryUuid = categoryId != null && !categoryId.isBlank() ? UUID.fromString(categoryId) : null;

        var productPage = productRepository.findAllWithFilters(
            search, categoryUuid, true, null, page, size, "name", "asc"
        );

        var responses = productPage.content().stream()
            .map(product -> {
                var category = product.getCategoryId() != null
                    ? categoryRepository.findById(product.getCategoryId()).orElse(null)
                    : null;

                var stocks = warehouseStockRepository.findByProductId(product.getId());
                int totalStock = stocks.stream().mapToInt(s -> s.getQuantity()).sum();
                int availableStock = stocks.stream().mapToInt(s -> s.getAvailableQuantity()).sum();

                return PortalProductMapper.toResponse(product, category, totalStock, availableStock);
            })
            .toList();

        return new PageResponse<>(
            responses,
            productPage.totalElements(),
            productPage.totalPages(),
            productPage.size(),
            productPage.number(),
            productPage.first(),
            productPage.last(),
            productPage.empty()
        );
    }
}
