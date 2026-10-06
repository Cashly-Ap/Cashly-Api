package com.cashly.cashly_api.customers.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerWarehouseAccess {

    private final UUID id;
    private final UUID customerId;
    private final UUID warehouseId;
    private final LocalDateTime grantedAt;
    private final UUID grantedBy;

    public CustomerWarehouseAccess(UUID id, UUID customerId, UUID warehouseId,
                                    LocalDateTime grantedAt, UUID grantedBy) {
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (warehouseId == null) throw new IllegalArgumentException("Warehouse ID cannot be null");
        if (grantedBy == null) throw new IllegalArgumentException("GrantedBy cannot be null");

        this.id = id;
        this.customerId = customerId;
        this.warehouseId = warehouseId;
        this.grantedAt = grantedAt != null ? grantedAt : LocalDateTime.now();
        this.grantedBy = grantedBy;
    }

    public static CustomerWarehouseAccess create(UUID customerId, UUID warehouseId, UUID grantedBy) {
        return new CustomerWarehouseAccess(null, customerId, warehouseId, LocalDateTime.now(), grantedBy);
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getWarehouseId() { return warehouseId; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public UUID getGrantedBy() { return grantedBy; }
}
