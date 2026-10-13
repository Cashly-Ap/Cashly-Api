package com.cashly.cashly_api.categories.application.ports.out;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CategoryUsageGateway {
    long countActiveProductsByCategory(UUID categoryId);
    Map<UUID, Long> countActiveProductsByCategoryIds(Set<UUID> categoryIds);
}
