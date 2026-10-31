package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface StockDeductionGateway {
    void deduct(List<Allocation> allocations);
    record Allocation(UUID warehouseId, UUID productId, int quantity) {
    }
}
