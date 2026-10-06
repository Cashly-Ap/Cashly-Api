package com.cashly.cashly_api.reports.orders.application.usecases;

import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.reports.orders.application.port.OrderPdfPort;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class GenerateOrderPdfUseCaseImpl implements GenerateOrderPdfUseCase {

    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderPdfPort orderPdfPort;

    public GenerateOrderPdfUseCaseImpl(OrderRepository orderRepository,
                                        WarehouseStockRepository warehouseStockRepository,
                                        WarehouseRepository warehouseRepository,
                                        OrderPdfPort orderPdfPort) {
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
        this.orderPdfPort = orderPdfPort;
    }

    @Override
    public byte[] execute(UUID orderId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Map<OrderMapper.WarehouseProductKey, OrderMapper.WarehouseEnrichment> enrichmentMap =
                buildEnrichmentMap(order);

        OrderDetailResponse orderDetail = OrderMapper.toDetailResponse(order, enrichmentMap, Map.of());

        return orderPdfPort.generatePdf(orderDetail);
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