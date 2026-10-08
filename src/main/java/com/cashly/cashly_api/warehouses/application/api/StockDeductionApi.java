package com.cashly.cashly_api.warehouses.application.api;

import java.util.List;
import java.util.UUID;

public interface StockDeductionApi {
    void deduct(List<Allocation> allocations);
    record Allocation(UUID warehouseId, UUID productId, int quantity) {}
}
