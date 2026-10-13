package com.cashly.cashly_api.warehouses.application.ports.out;

import java.util.Set;
import java.util.UUID;

public interface CustomerWarehouseAccessGateway {
    Set<UUID> accessibleWarehouseIds(UUID customerId);
}
