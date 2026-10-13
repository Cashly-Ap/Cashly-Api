package com.cashly.cashly_api.products.application.ports.in;

import com.cashly.cashly_api.shared.dto.PageResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface PurchaseProductCatalogQuery {
    PageResponse<Entry> find(String search, UUID categoryId, int page, int size);

    record Entry(UUID id, String code, String name, UUID categoryId, boolean active, BigDecimal purchasePrice) {}
}
