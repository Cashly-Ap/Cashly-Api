package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.products.application.dto.response.ProductListResponse;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListProductsUseCase {

    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductMapper productMapper;

    public ListProductsUseCase(ProductRepository productRepository,
                                ProductCategoryGateway categoryRepository,
                                ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> execute(String search, UUID categoryId, Boolean active,
                                                       Boolean composite, int page, int size,
                                                       String orderBy, String orderDir) {
        PageResponse<com.cashly.cashly_api.products.domain.entities.Product> result =
            productRepository.findAllWithFilters(search, categoryId, active, composite, page, size, orderBy, orderDir);

        Set<UUID> productIds = result.content().stream()
            .map(com.cashly.cashly_api.products.domain.entities.Product::getId)
            .collect(Collectors.toSet());

        Set<UUID> idsWithReferences = productRepository.findIdsWithReferences(productIds);

        List<ProductListResponse> content = result.content().stream()
            .map(product -> {
                String categoryName = categoryRepository.findById(product.getCategoryId())
                    .map(c -> c.name()).orElse("");
                boolean deletable = !idsWithReferences.contains(product.getId());
                return productMapper.toListResponse(product, categoryName, deletable);
            })
            .toList();

        return new PageResponse<>(content, result.totalElements(), result.totalPages(),
                result.size(), result.number(), result.first(), result.last(), result.empty());
    }
}
