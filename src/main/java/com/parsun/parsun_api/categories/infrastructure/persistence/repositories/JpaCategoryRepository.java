package com.parsun.parsun_api.categories.infrastructure.persistence.repositories;

import com.parsun.parsun_api.categories.domain.entities.Category;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.categories.infrastructure.persistence.entities.CategoryEntity;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaCategoryRepository implements CategoryRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("name", "createdAt", "updatedAt");

    private final SpringDataCategoryRepository springDataRepository;

    public JpaCategoryRepository(SpringDataCategoryRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Category save(Category category) {
        if (category.getId() != null) {
            return springDataRepository.findById(category.getId())
                .map(entity -> {
                    entity.updateFromDomain(category);
                    return springDataRepository.save(entity).toDomain();
                })
                .orElseGet(() -> {
                    CategoryEntity entity = CategoryEntity.fromDomain(category);
                    return springDataRepository.save(entity).toDomain();
                });
        }
        CategoryEntity entity = CategoryEntity.fromDomain(category);
        entity.setId(UUID.randomUUID());
        return springDataRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return springDataRepository.findById(id)
            .map(CategoryEntity::toDomain);
    }

    @Override
    public List<Category> findAllById(Set<UUID> ids) {
        return springDataRepository.findAllById(ids).stream()
            .map(CategoryEntity::toDomain)
            .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return springDataRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID id) {
        return springDataRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public long countActiveProductsByCategory(UUID categoryId) {
        return springDataRepository.countActiveProductsByCategory(categoryId);
    }

    @Override
    public PageResponse<CategorySummary> findAllWithFilters(String search, Boolean active,
                                                              int page, int size,
                                                              String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "name";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<CategoryEntity> result = springDataRepository.findAllWithFilters(search, active, pageable);

        Set<UUID> ids = result.stream().map(CategoryEntity::getId).collect(Collectors.toSet());
        Map<UUID, Long> productCounts = springDataRepository.countActiveProductsByCategoryIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        Page<CategorySummary> mapped = result.map(entity -> {
            Category category = entity.toDomain();
            long count = productCounts.getOrDefault(entity.getId(), 0L);
            return new CategorySummary(category, count);
        });

        return PageResponse.from(mapped);
    }
}
