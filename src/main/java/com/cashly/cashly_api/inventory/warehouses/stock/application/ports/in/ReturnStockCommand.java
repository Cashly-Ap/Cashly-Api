package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ReturnStockCommand(UUID returnId, String returnNumber,
        String userName, List<Allocation> allocations) {
    public ReturnStockCommand {
        Objects.requireNonNull(returnId);
        Objects.requireNonNull(returnNumber);
        allocations = List.copyOf(allocations);
    }

    public record Allocation(UUID warehouseId, UUID productId, int quantity) {
        public Allocation {
            Objects.requireNonNull(warehouseId);
            Objects.requireNonNull(productId);
            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Stock quantity must be positive");
            }
        }
    }
}
