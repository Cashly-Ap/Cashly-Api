package com.parsun.parsun_api.products.domain.repositories;

import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.shared.dto.PageResponse;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    boolean existsByCategoryId(UUID categoryId);
    PageResponse<Product> findAllWithFilters(String search, UUID categoryId, Boolean active,
                                              Boolean composite, int page, int size,
                                              String orderBy, String orderDir);
    void delete(UUID id);
}
