package com.cashly.cashly_api.invoices.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceLineAllocation {

    private final UUID id;
    private final UUID warehouseId;
    private final BigDecimal allocated;

    private InvoiceLineAllocation(UUID id, UUID warehouseId, BigDecimal allocated) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.allocated = allocated;
    }

    public static InvoiceLineAllocation create(UUID warehouseId, BigDecimal allocated) {
        return new InvoiceLineAllocation(UUID.randomUUID(), warehouseId, allocated);
    }

    public static InvoiceLineAllocation reconstitute(UUID id, UUID warehouseId, BigDecimal allocated) {
        return new InvoiceLineAllocation(id, warehouseId, allocated);
    }

    public UUID getId() { return id; }
    public UUID getWarehouseId() { return warehouseId; }
    public BigDecimal getAllocated() { return allocated; }
}
