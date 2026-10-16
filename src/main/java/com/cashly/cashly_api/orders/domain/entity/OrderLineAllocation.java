package com.cashly.cashly_api.orders.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderLineAllocation {

    private final UUID id;
    private final UUID warehouseId;
    private final BigDecimal allocated;

    private OrderLineAllocation(UUID id, UUID warehouseId, BigDecimal allocated) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.allocated = allocated;
    }

    public static OrderLineAllocation create(UUID warehouseId, BigDecimal allocated) {
        return new OrderLineAllocation(UUID.randomUUID(), warehouseId, allocated);
    }

    public static OrderLineAllocation reconstitute(UUID id, UUID warehouseId, BigDecimal allocated) {
        return new OrderLineAllocation(id, warehouseId, allocated);
    }

    public UUID getId() { return id; }
    public UUID getWarehouseId() { return warehouseId; }
    public BigDecimal getAllocated() { return allocated; }
}
