package com.cashly.cashly_api.quotes.application.contracts;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface QuoteWarehouseGateway {
    Optional<Warehouse> findById(UUID warehouseId);

    Set<UUID> accessibleWarehouseIds(UUID customerId);

    List<Stock> findStockByProductId(UUID productId);

    record Warehouse(UUID id, String name, String address) {}

    record Stock(UUID warehouseId, String warehouseName, int available) {}
}
