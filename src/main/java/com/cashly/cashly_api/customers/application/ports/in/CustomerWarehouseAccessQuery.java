package com.cashly.cashly_api.customers.application.ports.in;

import java.util.Set;
import java.util.UUID;

public interface CustomerWarehouseAccessQuery {
    Set<UUID> accessibleWarehouseIds(UUID customerId);
}
