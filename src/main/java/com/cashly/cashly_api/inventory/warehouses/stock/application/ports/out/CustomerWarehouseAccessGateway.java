package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out;

import java.util.Set;
import java.util.UUID;

public interface CustomerWarehouseAccessGateway {
    Set<UUID> accessibleWarehouseIds(UUID customerId);
}
