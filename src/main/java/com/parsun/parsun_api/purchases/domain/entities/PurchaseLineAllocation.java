package com.parsun.parsun_api.purchases.domain.entities;

import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseLineAllocationId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class PurchaseLineAllocation {

    private final PurchaseLineAllocationId id;
    private final UUID warehouseId;
    private final BigDecimal quantity;

    private PurchaseLineAllocation(PurchaseLineAllocationId id, UUID warehouseId, BigDecimal quantity) {
        Objects.requireNonNull(id, "Allocation id cannot be null");
        Objects.requireNonNull(warehouseId, "Warehouse id cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Allocation quantity must be greater than zero");
        }
        this.id = id;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
    }

    public static PurchaseLineAllocation create(UUID warehouseId, BigDecimal quantity) {
        return new PurchaseLineAllocation(PurchaseLineAllocationId.generate(), warehouseId, quantity);
    }

    public static PurchaseLineAllocation reconstitute(PurchaseLineAllocationId id, UUID warehouseId, BigDecimal quantity) {
        return new PurchaseLineAllocation(id, warehouseId, quantity);
    }

    public PurchaseLineAllocationId getId() { return id; }
    public UUID getWarehouseId() { return warehouseId; }
    public BigDecimal getQuantity() { return quantity; }
}
