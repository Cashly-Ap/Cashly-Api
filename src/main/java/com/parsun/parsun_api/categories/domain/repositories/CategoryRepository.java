package com.parsun.parsun_api.categories.domain.repositories;

import com.parsun.parsun_api.categories.domain.entities.Category;
import com.parsun.parsun_api.shared.dto.PageResponse;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    long countActiveProductsByCategory(UUID categoryId);
    PageResponse<CategorySummary> findAllWithFilters(String search, Boolean active,
                                                      int page, int size,
                                                      String orderBy, String orderDir);

    record CategorySummary(Category category, long productCount) {}
}
