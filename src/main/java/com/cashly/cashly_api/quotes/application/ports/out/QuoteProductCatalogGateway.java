package com.cashly.cashly_api.quotes.application.ports.out;

import com.cashly.cashly_api.shared.dto.PageResponse;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface QuoteProductCatalogGateway {
    Optional<ProductReference> findProduct(UUID productId);

    Set<UUID> findProductIdsByCurrentCategory(UUID categoryId);

    PageResponse<ProductEntry> findForCustomer(UUID customerId, String search, UUID categoryId,
                                                Boolean composite, int page, int size);

    record ProductReference(UUID id, String code, String name, UUID categoryId) {}

    record ProductEntry(UUID id, String code, String name, UUID categoryId, String categoryName,
                        boolean composite, boolean active, String photo, BigDecimal customerPrice) {}
}
