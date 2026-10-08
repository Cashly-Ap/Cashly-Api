package com.cashly.cashly_api.purchases.application.contracts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseWarehouseGateway {
    Optional<Warehouse> findById(UUID id);
    List<Warehouse> activeWarehouses();
    List<Stock> findByProductId(UUID productId);
    record Warehouse(UUID id, String code, String name) {}
    record Stock(UUID warehouseId, int quantity, int reserved, int available) {}
}