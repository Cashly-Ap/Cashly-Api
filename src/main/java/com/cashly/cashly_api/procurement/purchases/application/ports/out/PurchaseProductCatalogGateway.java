package com.cashly.cashly_api.procurement.purchases.application.ports.out;

import com.cashly.cashly_api.shared.dto.PageResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface PurchaseProductCatalogGateway {
    PageResponse<Entry> find(String search, UUID categoryId, int page, int size);

    record Entry(UUID id, String code, String name, UUID categoryId, String categoryName,
                 boolean active, BigDecimal purchasePrice) {}
}
