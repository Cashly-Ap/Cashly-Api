package com.cashly.cashly_api.orders.application.contracts;

import java.util.List;
import java.util.UUID;

public interface OrderInventoryGateway {
    void deduct(List<Allocation> allocations);

    void reserve(List<Allocation> allocations);

    void release(List<Allocation> allocations);

    void deductReservations(List<Allocation> allocations, Reference reference);

    void restore(List<Allocation> allocations, Reference reference);

    record Allocation(UUID warehouseId, UUID productId, int quantity) {}

    record Reference(UUID id, String number, String description) {}
}
