package com.cashly.cashly_api.sales.orders.infrastructure.integration;

import com.cashly.cashly_api.sales.orders.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.StockAvailabilityQuery;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderStockAvailabilityAdapter implements StockAvailabilityGateway {
    private final StockAvailabilityQuery query;

    public OrderStockAvailabilityAdapter(StockAvailabilityQuery query) {
        this.query = query;
    }

    @Override
    public List<EnrichedStock> enrich(List<AllocationData> allocations) {
        var command = allocations.stream().map(item -> new StockAvailabilityQuery.AllocationData(
                item.warehouseId(), item.productId(), item.allocated())).toList();
        return query.enrich(command).stream().map(item -> new EnrichedStock(item.warehouseId(),
                item.productId(), item.warehouseCode(), item.warehouseName(), item.availableStock())).toList();
    }

    @Override
    public Map<UUID, List<WarehouseStockInfo>> getAllStockByProducts(UUID customerId, List<UUID> productIds) {
        return query.getAllStockByProducts(customerId, productIds).entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, entry -> entry.getValue().stream().map(item -> new WarehouseStockInfo(
                        item.warehouseId(), item.warehouseCode(), item.warehouseName(),
                        item.quantity(), item.reserved(), item.available())).toList()));
    }
}