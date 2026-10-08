package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.contracts.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.contracts.OrderNotificationGateway;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
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
class ConfirmOrderUseCaseUnitTest {
    @Mock private OrderRepository orders;
    @Mock private OrderInventoryGateway inventory;
    @Mock private OrderNotificationGateway notifications;
    private ConfirmOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConfirmOrderUseCase(orders, inventory, notifications);
    }

    @Test
    void confirmsOrderAndDeductsItsReservationsThroughContract() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Order order = order(id, OrderStatus.DRAFT, productId, warehouseId);
        when(orders.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(id);

        assertEquals("CONFIRMED", result.status());
        verify(inventory).deductReservations(List.of(new OrderInventoryGateway.Allocation(warehouseId, productId, 4)),
                new OrderInventoryGateway.Reference(id, "ORD-2026-00001", "Order confirmed"));
        verify(notifications).notifyCustomerOrderApproved(order.getCustomerId(), id);
    }

    @Test
    void failsWhenOrderDoesNotExistWithoutCallingExternalContracts() {
        UUID id = UUID.randomUUID();
        when(orders.findById(OrderId.of(id))).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> useCase.execute(id));

        verifyNoInteractions(inventory, notifications);
    }

    private Order order(UUID id, OrderStatus status, UUID productId, UUID warehouseId) {
        var line = OrderLine.create(productId, "Product", "SKU", BigDecimal.valueOf(4), BigDecimal.TEN,
                List.of(OrderLineAllocation.create(warehouseId, BigDecimal.valueOf(4))));
        return Order.reconstitute(OrderId.of(id), OrderNumber.of(2026, 1), UUID.randomUUID(), "Customer", "RIF",
                UUID.randomUUID(), "Actor", status, null, null, null, null, null, List.of(line),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
