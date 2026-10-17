package com.cashly.cashly_api.sales.orders.application.ports.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StockAvailabilityGateway {
    Map<UUID, List<WarehouseStockInfo>> getAllStockByProducts(UUID customerId, List<UUID> productIds);
    List<EnrichedStock> enrich(List<AllocationData> allocations);
    record AllocationData(UUID warehouseId, UUID productId, int allocated) {}
    record EnrichedStock(UUID warehouseId, UUID productId, String warehouseCode, String warehouseName, int availableStock) {}
    record WarehouseStockInfo(UUID warehouseId, String warehouseCode, String warehouseName, int quantity, int reserved, int available) {}
}
