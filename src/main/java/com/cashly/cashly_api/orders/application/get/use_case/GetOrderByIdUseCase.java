package com.cashly.cashly_api.orders.application.get.use_case;

import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.entity.Order;
import com.cashly.cashly_api.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.orders.application.ports.out.CustomerCreditGateway;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetOrderByIdUseCase {

    private final OrderRepository orderRepository;
    private final StockAvailabilityGateway stockEnrichmentService;
    private final CustomerCreditGateway creditGuard;

    public GetOrderByIdUseCase(OrderRepository orderRepository,
                                StockAvailabilityGateway stockEnrichmentService,
                                CustomerCreditGateway creditGuard) {
        this.orderRepository = orderRepository;
        this.stockEnrichmentService = stockEnrichmentService;
        this.creditGuard = creditGuard;
    }

    public OrderDetailResponse execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        BigDecimal creditLimit = BigDecimal.ZERO;
        BigDecimal availableCredit = BigDecimal.ZERO;
        try {
            var creditStatus = creditGuard.getCreditLimits(order.getCustomerId());
            creditLimit = creditStatus.creditLimit();
            availableCredit = creditStatus.availableCredit();
        } catch (Exception ignored) {
        }

        List<StockAvailabilityGateway.AllocationData> allocations = order.getLines().stream()
                .flatMap(line -> line.getAllocations().stream()
                        .map(a -> new StockAvailabilityGateway.AllocationData(
                                a.getWarehouseId(),
                                line.getProductId(),
                                a.getAllocated().intValue()
                        )))
                .toList();

        Map<OrderMapper.WarehouseProductKey, Integer> thisOrderAllocations = new HashMap<>();
        for (StockAvailabilityGateway.AllocationData ad : allocations) {
            OrderMapper.WarehouseProductKey key = new OrderMapper.WarehouseProductKey(ad.warehouseId(), ad.productId());
            thisOrderAllocations.merge(key, ad.allocated(), Integer::sum);
        }

        List<StockAvailabilityGateway.EnrichedStock> enriched = stockEnrichmentService.enrich(allocations);

        Map<OrderMapper.WarehouseProductKey, OrderMapper.WarehouseEnrichment> enrichmentMap = new HashMap<>();
        for (StockAvailabilityGateway.EnrichedStock es : enriched) {
            enrichmentMap.put(
                    new OrderMapper.WarehouseProductKey(es.warehouseId(), es.productId()),
                    new OrderMapper.WarehouseEnrichment(es.warehouseCode(), es.warehouseName(), es.availableStock())
            );
        }

        List<UUID> productIds = order.getLines().stream()
                .map(OrderLine::getProductId)
                .toList();
        Map<UUID, List<StockAvailabilityGateway.WarehouseStockInfo>> productStocks =
                stockEnrichmentService.getAllStockByProducts(order.getCustomerId(), productIds);

        return OrderMapper.toDetailResponse(order, enrichmentMap, productStocks, thisOrderAllocations,
                creditLimit, availableCredit);
    }
}
