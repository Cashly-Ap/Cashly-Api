package com.cashly.cashly_api.sales.invoices.invoices.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockDeductionGateway {
    void deduct(List<Allocation> allocations);
    record Allocation(UUID warehouseId, UUID productId, BigDecimal quantity) {
    }
}
