package com.cashly.cashly_api.sales.orders.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StockAvailabilityGateway {
    Map<UUID, List<WarehouseStockInfo>> getAllStockByProducts(UUID customerId,
            List<UUID> productIds);
    List<EnrichedStock> enrich(List<AllocationData> allocations);
    record AllocationData(UUID warehouseId, UUID productId,
            BigDecimal allocated) {
    }
    record EnrichedStock(UUID warehouseId, UUID productId, String warehouseCode,
            String warehouseName, BigDecimal availableStock) {
    }
    record WarehouseStockInfo(UUID warehouseId, String warehouseCode,
            String warehouseName, BigDecimal quantity, BigDecimal reserved,
            BigDecimal available) {
    }
}
