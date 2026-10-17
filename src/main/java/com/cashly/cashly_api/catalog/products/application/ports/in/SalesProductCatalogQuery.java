package com.cashly.cashly_api.catalog.products.application.ports.in;

import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SalesProductCatalogQuery {
    Optional<ProductReference> findProduct(UUID productId);

    Set<UUID> findProductIdsByCurrentCategory(UUID categoryId);

    PageResponse<SalesProductCatalogEntry> findForTransaction(UUID customerId, String search,
                                                               UUID categoryId, Boolean composite,
                                                               Boolean withStock, int page, int size);

    record ProductReference(UUID id, String code, String name, UUID categoryId) {}
}
