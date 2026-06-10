package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.exceptions.OrderCannotBeDeletedException;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
    private WarehouseStockRepository warehouseStockRepository;

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

        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 10,
                LocalDateTime.now(), LocalDateTime.now());

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        deleteOrderUseCase.execute(id);

        assertEquals(5, stock.getReserved()); 
        verify(warehouseStockRepository).save(stock);
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

        WarehouseStock stock1 = new WarehouseStock(UUID.randomUUID(), warehouseId1, productId, 30, 5,
                LocalDateTime.now(), LocalDateTime.now());
        WarehouseStock stock2 = new WarehouseStock(UUID.randomUUID(), warehouseId2, productId, 20, 10,
                LocalDateTime.now(), LocalDateTime.now());

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId1, productId))
                .thenReturn(Optional.of(stock1));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId2, productId))
                .thenReturn(Optional.of(stock2));

        deleteOrderUseCase.execute(id);

        assertEquals(2, stock1.getReserved()); 
        assertEquals(3, stock2.getReserved()); 
        verify(warehouseStockRepository, times(2)).save(any(WarehouseStock.class));
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
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.empty());

        deleteOrderUseCase.execute(id);

        verify(warehouseStockRepository, never()).save(any());
        verify(orderRepository).deleteById(OrderId.of(id));
    }
}
