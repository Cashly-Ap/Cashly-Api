package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GetOrderByIdUseCase {

    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;

    public GetOrderByIdUseCase(OrderRepository orderRepository,
                                WarehouseStockRepository warehouseStockRepository,
                                WarehouseRepository warehouseRepository) {
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public OrderDetailResponse execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        Map<OrderMapper.WarehouseProductKey, OrderMapper.WarehouseEnrichment> enrichmentMap =
                buildEnrichmentMap(order);

        return OrderMapper.toDetailResponse(order, enrichmentMap);
    }

    private Map<OrderMapper.WarehouseProductKey, OrderMapper.WarehouseEnrichment> buildEnrichmentMap(Order order) {
        Map<UUID, Warehouse> warehouseCache = new HashMap<>();
        Map<OrderMapper.WarehouseProductKey, OrderMapper.WarehouseEnrichment> result = new HashMap<>();

        for (OrderLine line : order.getLines()) {
            UUID productId = line.getProductId();

            for (OrderLineAllocation allocation : line.getAllocations()) {
                UUID warehouseId = allocation.getWarehouseId();

                Warehouse warehouse = warehouseCache.computeIfAbsent(
                        warehouseId,
                        wid -> warehouseRepository.findById(wid).orElse(null)
                );
                if (warehouse == null) continue;

                int currentStock = warehouseStockRepository
                        .findByWarehouseIdAndProductId(warehouseId, productId)
                        .map(WarehouseStock::getQuantity)
                        .orElse(0);

                int allocated = allocation.getAllocated().intValue();
                int availableStock = currentStock + allocated;

                OrderMapper.WarehouseProductKey key = new OrderMapper.WarehouseProductKey(warehouseId, productId);
                result.put(key, new OrderMapper.WarehouseEnrichment(
                        warehouse.getCode(),
                        warehouse.getName(),
                        availableStock
                ));
            }
        }

        return result;
    }
}
