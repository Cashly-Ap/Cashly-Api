package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.exceptions.OrderCannotBeDeletedException;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.orders.application.contracts.OrderInventoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteOrderUseCase Unit Tests")
class DeleteOrderUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderInventoryGateway warehouseStockRepository;

    private DeleteOrderUseCase deleteOrderUseCase;

    @BeforeEach
    void setUp() {
        deleteOrderUseCase = new DeleteOrderUseCase(orderRepository, warehouseStockRepository);
    }

    @Test
    void should_DeleteDraftOrder_When_OrderExists() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLineAllocation allocation = OrderLineAllocation.create(warehouseId, BigDecimal.valueOf(5));
        OrderLine line = OrderLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Order order = Order.reconstitute(
                OrderId.of(id),
                OrderNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator Name",
                OrderStatus.DRAFT,
                null, null, null, null, null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));

        deleteOrderUseCase.execute(id);

        verify(warehouseStockRepository).release(List.of(new OrderInventoryGateway.Allocation(warehouseId, productId, 5)));
        verify(orderRepository).deleteById(OrderId.of(id));
    }

    @Test
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> deleteOrderUseCase.execute(id)
        );

        verify(orderRepository, never()).deleteById(any());
        verifyNoInteractions(warehouseStockRepository);
    }

    @Test
    void should_ThrowOrderCannotBeDeletedException_When_OrderIsNotDraft() {
        UUID id = UUID.randomUUID();
        Order order = Order.reconstitute(
                OrderId.of(id),
                OrderNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator Name",
                OrderStatus.CONFIRMED,
                null, null, null, null, null,
                List.of(OrderLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                        BigDecimal.ONE, BigDecimal.TEN, List.of())),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));

        assertThrows(
                OrderCannotBeDeletedException.class,
                () -> deleteOrderUseCase.execute(id)
        );

        verify(orderRepository, never()).deleteById(any());
        verifyNoInteractions(warehouseStockRepository);
    }

    @Test
    void should_ReleaseReservedStockForAllAllocations_When_DeletingDraftOrder() {
        UUID id = UUID.randomUUID();
        UUID warehouseId1 = UUID.randomUUID();
        UUID warehouseId2 = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLineAllocation alloc1 = OrderLineAllocation.create(warehouseId1, BigDecimal.valueOf(3));
        OrderLineAllocation alloc2 = OrderLineAllocation.create(warehouseId2, BigDecimal.valueOf(7));
        OrderLine line = OrderLine.create(productId, "Product A", "PROD-001",
                BigDecimal.TEN, BigDecimal.TEN, List.of(alloc1, alloc2));
        Order order = Order.reconstitute(
                OrderId.of(id),
                OrderNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator Name",
                OrderStatus.DRAFT,
                null, null, null, null, null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));

        deleteOrderUseCase.execute(id);

        verify(warehouseStockRepository).release(List.of(
                new OrderInventoryGateway.Allocation(warehouseId1, productId, 3),
                new OrderInventoryGateway.Allocation(warehouseId2, productId, 7)));
        verify(orderRepository).deleteById(OrderId.of(id));
    }

    @Test
    void should_NotFail_When_StockNotFoundForAllocation() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLineAllocation allocation = OrderLineAllocation.create(warehouseId, BigDecimal.valueOf(5));
        OrderLine line = OrderLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Order order = Order.reconstitute(
                OrderId.of(id),
                OrderNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator Name",
                OrderStatus.DRAFT,
                null, null, null, null, null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        deleteOrderUseCase.execute(id);

        verify(warehouseStockRepository).release(List.of(new OrderInventoryGateway.Allocation(warehouseId, productId, 5)));
        verify(orderRepository).deleteById(OrderId.of(id));
    }
}
