package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetOrderByIdUseCase {

    private final OrderRepository orderRepository;
    private final StockEnrichmentService stockEnrichmentService;

    public GetOrderByIdUseCase(OrderRepository orderRepository,
                                StockEnrichmentService stockEnrichmentService) {
        this.orderRepository = orderRepository;
        this.stockEnrichmentService = stockEnrichmentService;
    }

    public OrderDetailResponse execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        List<StockEnrichmentService.AllocationData> allocations = order.getLines().stream()
                .flatMap(line -> line.getAllocations().stream()
                        .map(a -> new StockEnrichmentService.AllocationData(
                                a.getWarehouseId(),
                                line.getProductId(),
                                a.getAllocated().intValue()
                        )))
                .toList();

        List<StockEnrichmentService.EnrichedStock> enriched = stockEnrichmentService.enrich(allocations);

        Map<OrderMapper.WarehouseProductKey, OrderMapper.WarehouseEnrichment> enrichmentMap = new HashMap<>();
        for (StockEnrichmentService.EnrichedStock es : enriched) {
            enrichmentMap.put(
                    new OrderMapper.WarehouseProductKey(es.warehouseId(), es.productId()),
                    new OrderMapper.WarehouseEnrichment(es.warehouseCode(), es.warehouseName(), es.availableStock())
            );
        }

        List<UUID> productIds = order.getLines().stream()
                .map(OrderLine::getProductId)
                .toList();
        Map<UUID, List<StockEnrichmentService.WarehouseStockInfo>> productStocks =
                stockEnrichmentService.getAllStockByProducts(order.getCustomerId(), productIds);

        return OrderMapper.toDetailResponse(order, enrichmentMap, productStocks);
    }
}
