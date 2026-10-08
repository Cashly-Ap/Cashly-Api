package com.cashly.cashly_api.orders.application.contracts;

import com.cashly.cashly_api.shared.dto.PageResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface OrderProductCatalogGateway {
    Optional<ProductReference> findProduct(UUID productId);

    Set<UUID> findProductIdsByCurrentCategory(UUID categoryId);

    PageResponse<ProductEntry> findForCustomer(UUID customerId, String search, UUID categoryId,
                                                Boolean composite, Boolean withStock, int page, int size);

    record ProductReference(UUID id, String code, String name, UUID categoryId) {}

    record ProductEntry(UUID id, String code, String name, UUID categoryId, String categoryName,
                        boolean composite, boolean active, String photo, BigDecimal customerPrice,
                        int totalStock, List<Stock> stock) {
        public ProductEntry {
            stock = List.copyOf(stock);
        }
    }

    record Stock(UUID warehouseId, String warehouseCode, String warehouseName,
                 int quantity, int reserved, int available) {}
}
