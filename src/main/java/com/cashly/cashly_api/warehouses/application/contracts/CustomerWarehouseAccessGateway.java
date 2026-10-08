package com.cashly.cashly_api.warehouses.application.contracts;

import java.util.Set;
import java.util.UUID;

public interface CustomerWarehouseAccessGateway {
    Set<UUID> accessibleWarehouseIds(UUID customerId);
}
