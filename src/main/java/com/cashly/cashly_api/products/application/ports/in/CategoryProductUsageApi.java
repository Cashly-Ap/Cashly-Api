package com.cashly.cashly_api.products.application.ports.in;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CategoryProductUsageApi {
    long countActiveProductsByCategory(UUID categoryId);
    Map<UUID, Long> countActiveProductsByCategoryIds(Set<UUID> categoryIds);
}
