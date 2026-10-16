package com.cashly.cashly_api.warehouses.stock.application.ports.in;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record InvoiceStockCommand(UUID invoiceId, String invoiceNumber, List<Allocation> allocations) {
    public InvoiceStockCommand {
        Objects.requireNonNull(invoiceId, "Invoice ID cannot be null");
        Objects.requireNonNull(invoiceNumber, "Invoice number cannot be null");
        allocations = List.copyOf(allocations);
    }
    public record Allocation(UUID warehouseId, UUID productId, int quantity) {
        public Allocation {
            Objects.requireNonNull(warehouseId, "Warehouse ID cannot be null");
            Objects.requireNonNull(productId, "Product ID cannot be null");
            if (quantity <= 0) throw new IllegalArgumentException("Allocation quantity must be positive");
        }
    }
}
