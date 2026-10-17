package com.cashly.cashly_api.procurement.purchases.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface InventoryGateway {
    void receive(UUID purchaseId, String purchaseNumber, List<Allocation> allocations);
    void reverseReceipt(UUID purchaseId, String purchaseNumber, List<Allocation> allocations);

    record Allocation(UUID warehouseId, UUID productId, int quantity) { }
}
