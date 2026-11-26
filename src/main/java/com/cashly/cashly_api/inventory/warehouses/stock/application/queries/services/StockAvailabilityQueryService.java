package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.services;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.StockAvailabilityQuery;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.CustomerWarehouseAccessGateway;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class StockAvailabilityQueryService implements StockAvailabilityQuery {

    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerWarehouseAccessGateway accessGateway;

    public StockAvailabilityQueryService(
            WarehouseStockRepository warehouseStockRepository,
            WarehouseRepository warehouseRepository,
            CustomerWarehouseAccessGateway accessGateway) {
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
        this.accessGateway = accessGateway;
    }

    @Override
    public Map<UUID, List<WarehouseStockInfo>> getAllStockByProducts(
            UUID customerId, List<UUID> productIds) {
        Set<UUID> uniqueProductIds = new HashSet<>(productIds);

        Set<UUID> accessibleWarehouseIds = accessGateway
                .accessibleWarehouseIds(customerId);

        Map<UUID, Warehouse> warehouseCache = new HashMap<>();
        Map<UUID, List<WarehouseStockInfo>> result = new HashMap<>();

        for (UUID productId : uniqueProductIds) {
            List<WarehouseStock> stocks = warehouseStockRepository
                    .findByProductId(productId);
            List<WarehouseStockInfo> stockInfos = new ArrayList<>();

            for (WarehouseStock stock : stocks) {
                if (!accessibleWarehouseIds.contains(stock.getWarehouseId()))
                    continue;

                Warehouse warehouse = warehouseCache.computeIfAbsent(
                        stock.getWarehouseId(),
                        wid -> warehouseRepository.findById(wid).orElse(null));
                if (warehouse == null)
                    continue;

                stockInfos.add(new WarehouseStockInfo(stock.getWarehouseId(),
                        warehouse.getCode(), warehouse.getName(),
                        stock.getQuantity(), stock.getReserved(),
                        stock.getAvailableQuantity()));
            }

            result.put(productId, stockInfos);
        }

        return result;
    }

    @Override
    public List<EnrichedStock> enrich(List<AllocationData> allocations) {
        Map<Key, BigDecimal> groupedAllocations = allocations.stream()
                .collect(Collectors.groupingBy(
                        a -> new Key(a.warehouseId(), a.productId()),
                        Collectors.reducing(BigDecimal.ZERO,
                                AllocationData::allocated, BigDecimal::add)));

        Map<UUID, Warehouse> warehouseCache = new HashMap<>();
        List<EnrichedStock> result = new ArrayList<>();

        for (Map.Entry<Key, BigDecimal> entry : groupedAllocations.entrySet()) {
            Key key = entry.getKey();
            BigDecimal totalAllocated = entry.getValue();

            Warehouse warehouse = warehouseCache.computeIfAbsent(
                    key.warehouseId(),
                    wid -> warehouseRepository.findById(wid).orElse(null));
            if (warehouse == null)
                continue;

            BigDecimal currentStock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(key.warehouseId(),
                            key.productId())
                    .map(WarehouseStock::getQuantity).orElse(BigDecimal.ZERO);

            BigDecimal availableStock = currentStock.add(totalAllocated);

            result.add(new EnrichedStock(key.warehouseId(), key.productId(),
                    warehouse.getCode(), warehouse.getName(), availableStock));
        }

        return result;
    }

    private record Key(UUID warehouseId, UUID productId) {
    }
}
