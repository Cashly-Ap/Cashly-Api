package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in;

import java.util.List;
import java.util.UUID;

public interface StockDeductionApi {
    void deduct(List<Allocation> allocations);
    record Allocation(UUID warehouseId, UUID productId, int quantity) {
    }
}
