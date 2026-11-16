package com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.in;

import java.util.Set;
import java.util.UUID;

public interface CustomerWarehouseAccessQuery {
    Set<UUID> accessibleWarehouseIds(UUID customerId);
}
