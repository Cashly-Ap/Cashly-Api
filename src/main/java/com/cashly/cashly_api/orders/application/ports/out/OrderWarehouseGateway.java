package com.cashly.cashly_api.orders.application.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderWarehouseGateway {
    Optional<Warehouse> findById(UUID warehouseId);

    boolean hasCustomerAccess(UUID customerId, UUID warehouseId);

    record Warehouse(UUID id, String name) {}
}
