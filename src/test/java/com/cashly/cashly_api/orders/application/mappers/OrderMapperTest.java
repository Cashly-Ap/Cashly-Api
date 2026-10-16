package com.cashly.cashly_api.orders.application.mappers;

import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.domain.entity.Order;
import com.cashly.cashly_api.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.orders.domain.entity.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OrderMapper Unit Tests")
class OrderMapperTest {

    private final OrderId orderId = OrderId.generate();
    private final OrderNumber orderNumber = OrderNumber.of(2026, 1);
    private final UUID customerId = UUID.randomUUID();
    private final String customerName = "Test Customer";
    private final String customerRif = "J-12345678-9";
    private final UUID createdBy = UUID.randomUUID();
    private final String createdByName = "Test User";
    private final BigDecimal subtotal = new BigDecimal("1000.00");
    private final LocalDateTime now = LocalDateTime.now();

    private Order createOrderWithLines() {
        List<OrderLine> lines = List.of(
                OrderLine.reconstitute(
                        com.cashly.cashly_api.orders.domain.valueobjects.OrderLineId.generate(),
                        UUID.randomUUID(), "Product A", "P001",
                        new BigDecimal("2"), new BigDecimal("250.00"),
                        List.of(
                                OrderLineAllocation.reconstitute(
                                        UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("2"))
                        )
                ),
                OrderLine.reconstitute(
                        com.cashly.cashly_api.orders.domain.valueobjects.OrderLineId.generate(),
                        UUID.randomUUID(), "Product B", "P002",
                        new BigDecimal("1"), new BigDecimal("500.00"),
                        List.of()
                )
        );

        return Order.reconstitute(
                orderId, orderNumber, customerId, customerName, customerRif,
                createdBy, createdByName, OrderStatus.CONFIRMED, "Test notes",
                null, null, null, null,
                lines, now, now
        );
    }

    @Test
    void should_MapOrderToOrderListResponse_When_ValidOrder() {
        Order order = createOrderWithLines();

        OrderListResponse response = OrderMapper.toListResponse(order);

        assertNotNull(response);
        assertEquals(orderId.value(), response.id());
        assertEquals(orderNumber.value(), response.orderNumber());
        assertEquals(customerId, response.customerId());
        assertEquals(customerName, response.customerName());
        assertEquals(OrderStatus.CONFIRMED.name(), response.status());
        assertEquals(subtotal, response.subtotal());
        assertEquals(2, response.lineCount());
        assertNotNull(response.createdAt());
    }

    @Test
    void should_MapOrderToOrderDetailResponse_When_ValidOrder() {
        Order order = createOrderWithLines();

        OrderDetailResponse response = OrderMapper.toDetailResponse(order);

        assertNotNull(response);
        assertEquals(orderId.value(), response.id());
        assertEquals(orderNumber.value(), response.orderNumber());
        assertEquals(customerId, response.customerId());
        assertEquals(customerName, response.customerName());
        assertEquals(customerRif, response.customerRif());
        assertEquals(createdBy, response.createdBy());
        assertEquals(createdByName, response.createdByName());
        assertEquals(OrderStatus.CONFIRMED.name(), response.status());
        assertEquals(subtotal, response.subtotal());
        assertNotNull(response.lines());
        assertEquals(2, response.lines().size());
    }

    @Test
    void should_MapOrderLinesWithAllocations_When_EnrichmentMapProvided() {
        Order order = createOrderWithLines();
        UUID warehouseId = order.getLines().get(0).getAllocations().get(0).getWarehouseId();
        UUID productId = order.getLines().get(0).getProductId();

        OrderMapper.WarehouseProductKey key = new OrderMapper.WarehouseProductKey(warehouseId, productId);
        Map<OrderMapper.WarehouseProductKey, OrderMapper.WarehouseEnrichment> enrichmentMap = Map.of(
                key, new OrderMapper.WarehouseEnrichment("WH-01", "Main Warehouse", 50)
        );

        OrderDetailResponse response = OrderMapper.toDetailResponse(order, enrichmentMap, Map.of());

        assertNotNull(response);
        assertEquals(2, response.lines().size());
        OrderDetailResponse.OrderLineDetailResponse lineResponse = response.lines().get(0);
        assertEquals("Product A", lineResponse.productName());
        assertEquals(new BigDecimal("2"), lineResponse.quantity());
        assertEquals(new BigDecimal("250.00"), lineResponse.unitPrice());
        assertEquals(new BigDecimal("500.00"), lineResponse.lineTotal());
        assertEquals(1, lineResponse.warehouseAllocations().size());
        assertEquals("WH-01", lineResponse.warehouseAllocations().get(0).warehouseCode());
        assertEquals(50, lineResponse.warehouseAllocations().get(0).availableStock());
    }

    @Test
    void should_MapOrderWithEmptyLines_When_OrderHasNoLines() {
        Order order = Order.reconstitute(
                orderId, orderNumber, customerId, customerName, customerRif,
                createdBy, createdByName, OrderStatus.DRAFT, null,
                null, null, null, null,
                List.of(), now, now
        );

        OrderDetailResponse response = OrderMapper.toDetailResponse(order);

        assertNotNull(response);
        assertTrue(response.lines().isEmpty());
    }

    @Test
    void should_MapOrderWithInvoiceReference_When_InvoiceIdPresent() {
        UUID invoiceId = UUID.randomUUID();
        Order order = Order.reconstitute(
                orderId, orderNumber, customerId, customerName, customerRif,
                createdBy, createdByName, OrderStatus.INVOICED, null,
                invoiceId, "FAC-2026-0001", null, null,
                List.of(), now, now
        );

        OrderListResponse listResponse = OrderMapper.toListResponse(order);

        assertEquals(invoiceId, listResponse.invoiceId());
    }
}
