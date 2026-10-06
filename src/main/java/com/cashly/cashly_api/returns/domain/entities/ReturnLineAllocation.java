package com.cashly.cashly_api.returns.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

public class ReturnLineAllocation {

    private final UUID id;
    private final UUID warehouseId;
    private final BigDecimal allocated;

    private ReturnLineAllocation(UUID id, UUID warehouseId, BigDecimal allocated) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.allocated = allocated;
    }

    public static ReturnLineAllocation create(UUID warehouseId, BigDecimal allocated) {
        return new ReturnLineAllocation(UUID.randomUUID(), warehouseId, allocated);
    }

    public static ReturnLineAllocation reconstitute(UUID id, UUID warehouseId, BigDecimal allocated) {
        return new ReturnLineAllocation(id, warehouseId, allocated);
    }

    public UUID getId() { return id; }
    public UUID getWarehouseId() { return warehouseId; }
    public BigDecimal getAllocated() { return allocated; }
}
