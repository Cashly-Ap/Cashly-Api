package com.cashly.cashly_api.catalog.categories.infrastructure.integration;

import com.cashly.cashly_api.catalog.categories.application.ports.out.CategoryUsageGateway;
import com.cashly.cashly_api.catalog.products.application.ports.in.CategoryProductUsageApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class ProductCategoryUsageAdapter implements CategoryUsageGateway {
    private final CategoryProductUsageApi usageApi;

    public ProductCategoryUsageAdapter(CategoryProductUsageApi usageApi) {
        this.usageApi = usageApi;
    }

    @Override
    public long countActiveProductsByCategory(UUID categoryId) {
        return usageApi.countActiveProductsByCategory(categoryId);
    }

    @Override
    public Map<UUID, Long> countActiveProductsByCategoryIds(Set<UUID> categoryIds) {
        return usageApi.countActiveProductsByCategoryIds(categoryIds);
    }
}
