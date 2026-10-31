package com.cashly.cashly_api.sales.orders.application.cancel.use_case;

import com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderNotificationGateway;
import com.cashly.cashly_api.sales.orders.application.cancel.request.CancelOrderRequest;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLineAllocation;
import com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseUnitTest {
    @Mock
    private OrderRepository orders;
    @Mock
    private OrderInventoryGateway inventory;
    @Mock
    private OrderNotificationGateway notifications;
    private CancelOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelOrderUseCase(orders, inventory, notifications);
    }

    @Test
    void releasesReservationForDraftOrder() {
        Order order = order(OrderStatus.DRAFT);
        when(orders.findById(OrderId.of(order.getId().value())))
                .thenReturn(Optional.of(order));
        when(orders.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(order.getId().value(),
                new CancelOrderRequest("Requested"));

        assertEquals("CANCELLED", response.status());
        verify(inventory).release(allocations(order));
        verify(notifications).notifyCustomerOrderRejected(order.getCustomerId(),
                order.getId().value(), "Requested");
        verify(inventory, never()).restore(anyList(), any());
    }

    @Test
    void restoresStockForConfirmedOrder() {
        Order order = order(OrderStatus.CONFIRMED);
        when(orders.findById(OrderId.of(order.getId().value())))
                .thenReturn(Optional.of(order));
        when(orders.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(order.getId().value(),
                new CancelOrderRequest("Rejected"));

        verify(inventory).restore(allocations(order),
                new OrderInventoryGateway.Reference(order.getId().value(),
                        order.getOrderNumber().value(), "Order cancelled"));
        verify(inventory, never()).release(anyList());
    }

    @Test
    void failsWhenOrderIsMissing() {
        UUID id = UUID.randomUUID();
        when(orders.findById(OrderId.of(id))).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> useCase.execute(id, new CancelOrderRequest("Reason")));

        verifyNoInteractions(inventory, notifications);
    }

    private List<OrderInventoryGateway.Allocation> allocations(Order order) {
        return order.getLines().stream().flatMap(line -> line.getAllocations()
                .stream()
                .map(allocation -> new OrderInventoryGateway.Allocation(
                        allocation.getWarehouseId(), line.getProductId(),
                        allocation.getAllocated().intValue())))
                .toList();
    }

    private Order order(OrderStatus status) {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        var line = OrderLine.create(productId, "Product", "SKU",
                BigDecimal.valueOf(3), BigDecimal.TEN,
                List.of(OrderLineAllocation.create(warehouseId,
                        BigDecimal.valueOf(3))));
        return Order.reconstitute(OrderId.of(id), OrderNumber.of(2026, 2),
                UUID.randomUUID(), "Customer", "RIF", UUID.randomUUID(),
                "Actor", status, null, null, null, null, null, List.of(line),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
