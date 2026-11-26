package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockDeductionApi {
    void deduct(List<Allocation> allocations);
    record Allocation(UUID warehouseId, UUID productId, BigDecimal quantity) {
    }
}
