package com.cashly.cashly_api.warehouses.application.api;

import java.util.List;
import java.util.UUID;

public record PurchaseStockCommand(UUID purchaseId, String purchaseNumber, List<Allocation> allocations) {
    public PurchaseStockCommand {
        allocations = List.copyOf(allocations);
    }

    public record Allocation(UUID warehouseId, UUID productId, int quantity) { }
}
