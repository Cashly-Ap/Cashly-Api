package com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.queries;

import com.cashly.cashly_api.products.application.ports.in.CategoryProductUsageApi;
import com.cashly.cashly_api.products.application.ports.in.PriceTypeProductUsageApi;
import com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.repositories.SpringDataProductUsageRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class JpaProductUsageQueries implements CategoryProductUsageApi, PriceTypeProductUsageApi {
    private final SpringDataProductUsageRepository repository;

    public JpaProductUsageQueries(SpringDataProductUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public long countActiveProductsByCategory(UUID categoryId) {
        return repository.countActiveProductsByCategory(categoryId);
    }

    @Override
    public Map<UUID, Long> countActiveProductsByCategoryIds(Set<UUID> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return repository.countActiveProductsByCategoryIds(categoryIds).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).longValue()));
    }

    @Override
    public boolean isInUseByProducts(UUID priceTypeId) {
        return repository.isInUseByProducts(priceTypeId);
    }

    @Override
    public Set<UUID> findIdsInUseByProducts(Set<UUID> priceTypeIds) {
        return priceTypeIds.isEmpty() ? Set.of() : repository.findIdsInUseByProducts(priceTypeIds);
    }
}
