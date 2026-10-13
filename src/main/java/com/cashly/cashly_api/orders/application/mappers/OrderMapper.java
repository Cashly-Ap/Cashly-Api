package com.cashly.cashly_api.orders.application.mappers;

import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.application.ports.out.StockAvailabilityGateway;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderMapper {

    public record WarehouseProductKey(UUID warehouseId, UUID productId) {}

    public record WarehouseEnrichment(String code, String name, int availableStock) {}

    private OrderMapper() {}

    public static OrderDetailResponse toDetailResponse(Order order,
                                                           Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
                                                           Map<UUID, List<StockAvailabilityGateway.WarehouseStockInfo>> productStocks,
                                                           Map<WarehouseProductKey, Integer> thisOrderAllocations,
                                                           java.math.BigDecimal creditLimit,
                                                           java.math.BigDecimal availableCredit) {
        List<OrderDetailResponse.OrderLineDetailResponse> lineResponses = order.getLines().stream()
                .map(line -> toLineDetailResponse(line, enrichmentMap, productStocks, thisOrderAllocations))
                .toList();

        return new OrderDetailResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getCustomerRif(),
                order.getCreatedBy(),
                order.getCreatedByName(),
                order.getStatus().name(),
                order.getNotes(),
                order.getSubtotal(),
                order.getInvoiceId(),
                order.getInvoiceNumber(),
                order.getCancelledAt(),
                order.getCancelledReason(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                lineResponses,
                creditLimit,
                availableCredit
        );
    }

    public static OrderDetailResponse toDetailResponse(Order order,
                                                           Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
                                                           Map<UUID, List<StockAvailabilityGateway.WarehouseStockInfo>> productStocks) {
        return toDetailResponse(order, enrichmentMap, productStocks, Map.of());
    }

    public static OrderDetailResponse toDetailResponse(Order order,
                                                           Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
                                                           Map<UUID, List<StockAvailabilityGateway.WarehouseStockInfo>> productStocks,
                                                           Map<WarehouseProductKey, Integer> thisOrderAllocations) {
        return toDetailResponse(order, enrichmentMap, productStocks, thisOrderAllocations, null, null);
    }

    public static OrderDetailResponse toDetailResponse(Order order) {
        return toDetailResponse(order, Map.of(), Map.of(), Map.of());
    }

    public static OrderListResponse toListResponse(Order order) {
        return new OrderListResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getStatus().name(),
                order.getSubtotal(),
                order.getLines().size(),
                order.getInvoiceId(),
                order.getCreatedAt()
        );
    }

    private static OrderDetailResponse.OrderLineDetailResponse toLineDetailResponse(
            OrderLine line, Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
            Map<UUID, List<StockAvailabilityGateway.WarehouseStockInfo>> productStocks,
            Map<WarehouseProductKey, Integer> thisOrderAllocations) {

        List<OrderDetailResponse.OrderLineDetailResponse.AllocationDetail> allocationDetails =
                line.getAllocations().stream()
                        .map(a -> {
                            WarehouseProductKey key = new WarehouseProductKey(a.getWarehouseId(), line.getProductId());
                            WarehouseEnrichment enrichment = enrichmentMap.get(key);
                            String code = enrichment != null ? enrichment.code() : null;
                            String name = enrichment != null ? enrichment.name() : null;
                            int availableStock = enrichment != null ? enrichment.availableStock() : 0;
                            return new OrderDetailResponse.OrderLineDetailResponse.AllocationDetail(
                                    a.getWarehouseId(),
                                    code,
                                    name,
                                    a.getAllocated(),
                                    availableStock
                            );
                        })
                        .toList();

        List<OrderDetailResponse.WarehouseStockDetail> stockDetails =
                productStocks.getOrDefault(line.getProductId(), List.of()).stream()
                        .map(s -> {
                            WarehouseProductKey key = new WarehouseProductKey(s.warehouseId(), line.getProductId());
                            int thisAllocation = thisOrderAllocations.getOrDefault(key, 0);
                            return new OrderDetailResponse.WarehouseStockDetail(
                                    s.warehouseId(),
                                    s.warehouseCode(),
                                    s.warehouseName(),
                                    s.quantity(),
                                    Math.max(0, s.reserved() - thisAllocation),
                                    s.available() + thisAllocation
                            );
                        })
                        .toList();

        return new OrderDetailResponse.OrderLineDetailResponse(
                line.getId().value(),
                line.getProductId(),
                line.getProductName(),
                line.getProductCode(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineTotal(),
                allocationDetails,
                stockDetails
        );
    }
}
