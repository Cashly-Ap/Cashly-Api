package com.cashly.cashly_api.warehouses.stock.application.queries;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.StockAvailabilityQuery;
import com.cashly.cashly_api.warehouses.stock.application.ports.out.CustomerWarehouseAccessGateway;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;

import java.util.*;
import java.util.stream.Collectors;

public class StockAvailabilityQueryService implements StockAvailabilityQuery {

    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerWarehouseAccessGateway accessGateway;

    public StockAvailabilityQueryService(WarehouseStockRepository warehouseStockRepository,
                                   WarehouseRepository warehouseRepository,
                                   CustomerWarehouseAccessGateway accessGateway) {
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
        this.accessGateway = accessGateway;
    }



    @Override
    public Map<UUID, List<WarehouseStockInfo>> getAllStockByProducts(UUID customerId, List<UUID> productIds) {
        Set<UUID> uniqueProductIds = new HashSet<>(productIds);

        Set<UUID> accessibleWarehouseIds = accessGateway.accessibleWarehouseIds(customerId);

        Map<UUID, Warehouse> warehouseCache = new HashMap<>();
        Map<UUID, List<WarehouseStockInfo>> result = new HashMap<>();

        for (UUID productId : uniqueProductIds) {
            List<WarehouseStock> stocks = warehouseStockRepository.findByProductId(productId);
            List<WarehouseStockInfo> stockInfos = new ArrayList<>();

            for (WarehouseStock stock : stocks) {
                if (!accessibleWarehouseIds.contains(stock.getWarehouseId())) continue;

                Warehouse warehouse = warehouseCache.computeIfAbsent(
                        stock.getWarehouseId(),
                        wid -> warehouseRepository.findById(wid).orElse(null)
                );
                if (warehouse == null) continue;

                stockInfos.add(new WarehouseStockInfo(
                        stock.getWarehouseId(),
                        warehouse.getCode(),
                        warehouse.getName(),
                        stock.getQuantity(),
                        stock.getReserved(),
                        stock.getAvailableQuantity()
                ));
            }

            result.put(productId, stockInfos);
        }

        return result;
    }

    @Override
    public List<EnrichedStock> enrich(List<AllocationData> allocations) {
        Map<Key, Integer> groupedAllocations = allocations.stream()
                .collect(Collectors.groupingBy(
                        a -> new Key(a.warehouseId(), a.productId()),
                        Collectors.summingInt(AllocationData::allocated)
                ));

        Map<UUID, Warehouse> warehouseCache = new HashMap<>();
        List<EnrichedStock> result = new ArrayList<>();

        for (Map.Entry<Key, Integer> entry : groupedAllocations.entrySet()) {
            Key key = entry.getKey();
            int totalAllocated = entry.getValue();

            Warehouse warehouse = warehouseCache.computeIfAbsent(
                    key.warehouseId(),
                    wid -> warehouseRepository.findById(wid).orElse(null)
            );
            if (warehouse == null) continue;

            int currentStock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(key.warehouseId(), key.productId())
                    .map(WarehouseStock::getQuantity)
                    .orElse(0);

            int availableStock = currentStock + totalAllocated;

            result.add(new EnrichedStock(
                    key.warehouseId(),
                    key.productId(),
                    warehouse.getCode(),
                    warehouse.getName(),
                    availableStock
            ));
        }

        return result;
    }

    private record Key(UUID warehouseId, UUID productId) {}
}
