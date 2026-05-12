package com.parsun.parsun_api.shared.application;

import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.*;
import java.util.stream.Collectors;

public class StockEnrichmentService {

    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerWarehouseAccessRepository customerWarehouseAccessRepository;

    public StockEnrichmentService(WarehouseStockRepository warehouseStockRepository,
                                   WarehouseRepository warehouseRepository,
                                   CustomerWarehouseAccessRepository customerWarehouseAccessRepository) {
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
        this.customerWarehouseAccessRepository = customerWarehouseAccessRepository;
    }

    public record AllocationData(UUID warehouseId, UUID productId, int allocated) {}

    public record EnrichedStock(UUID warehouseId, UUID productId,
                                 String warehouseCode, String warehouseName,
                                 int availableStock) {}

    public record WarehouseStockInfo(UUID warehouseId, String warehouseCode,
                                      String warehouseName, int quantity) {}

    public Map<UUID, List<WarehouseStockInfo>> getAllStockByProducts(UUID customerId, List<UUID> productIds) {
        Set<UUID> uniqueProductIds = new HashSet<>(productIds);

        Set<UUID> accessibleWarehouseIds = customerWarehouseAccessRepository.findByCustomerId(customerId)
                .stream()
                .map(CustomerWarehouseAccess::getWarehouseId)
                .collect(Collectors.toSet());

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
                        stock.getQuantity()
                ));
            }

            result.put(productId, stockInfos);
        }

        return result;
    }

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
