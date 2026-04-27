package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.application.dto.response.ProductListResponse;
import com.parsun.parsun_api.products.application.mappers.ProductMapper;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class ListProductsUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ListProductsUseCase(ProductRepository productRepository,
                                CategoryRepository categoryRepository,
                                ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> execute(String search, UUID categoryId, Boolean active,
                                                      Boolean composite, int page, int size,
                                                      String orderBy, String orderDir) {
        PageResponse<com.parsun.parsun_api.products.domain.entities.Product> result =
            productRepository.findAllWithFilters(search, categoryId, active, composite, page, size, orderBy, orderDir);

        List<ProductListResponse> content = result.content().stream()
            .map(product -> {
                String categoryName = categoryRepository.findById(product.getCategoryId())
                    .map(c -> c.getName()).orElse("");
                return productMapper.toListResponse(product, categoryName);
            })
            .toList();

        return new PageResponse<>(content, result.totalElements(), result.totalPages(),
                result.size(), result.number(), result.first(), result.last(), result.empty());
    }
}
