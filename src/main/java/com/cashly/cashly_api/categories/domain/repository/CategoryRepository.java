package com.cashly.cashly_api.categories.domain.repository;

import com.cashly.cashly_api.categories.domain.entity.Category;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAllById(Set<UUID> ids);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    PageResponse<Category> findAllWithFilters(String search, Boolean active,
                                                      int page, int size,
                                                      String orderBy, String orderDir);

}
