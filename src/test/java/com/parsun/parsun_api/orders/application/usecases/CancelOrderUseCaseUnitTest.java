package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.request.CancelOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
@DisplayName("CancelOrderUseCase Unit Tests")
class CancelOrderUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Captor
    private ArgumentCaptor<StockMovement> stockMovementCaptor;

    private CancelOrderUseCase cancelOrderUseCase;

    @BeforeEach
    void setUp() {
        cancelOrderUseCase = new CancelOrderUseCase(orderRepository,
                warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_CancelDraftOrder_When_OrderExists() {
        UUID id = UUID.randomUUID();
        Order order = buildOrder(id, OrderStatus.DRAFT);
        CancelOrderRequest request = new CancelOrderRequest("Customer request");

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailResponse response = cancelOrderUseCase.execute(id, request);

        assertNotNull(response);
        assertEquals("CANCELLED", response.status());
        verify(orderRepository).findById(OrderId.of(id));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void should_CancelConfirmedOrder_When_OrderExists() {
        UUID id = UUID.randomUUID();
        Order order = buildOrder(id, OrderStatus.CONFIRMED);
        CancelOrderRequest request = new CancelOrderRequest("Supplier issue");

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailResponse response = cancelOrderUseCase.execute(id, request);

        assertEquals("CANCELLED", response.status());
    }

    @Test
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        CancelOrderRequest request = new CancelOrderRequest("Reason");

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> cancelOrderUseCase.execute(id, request)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_ThrowIllegalStateException_When_OrderIsInvoiced() {
        UUID id = UUID.randomUUID();
        Order order = buildOrder(id, OrderStatus.INVOICED);
        CancelOrderRequest request = new CancelOrderRequest("Reason");

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> cancelOrderUseCase.execute(id, request)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_OnlyReleaseReserved_When_CancellingDraftOrder() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLineAllocation allocation = OrderLineAllocation.create(warehouseId, BigDecimal.valueOf(5));
        OrderLine line = OrderLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Order order = Order.reconstitute(
                OrderId.of(id), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.DRAFT, null, null, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
        CancelOrderRequest request = new CancelOrderRequest("Reason");

        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 10,
                LocalDateTime.now(), LocalDateTime.now());

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        cancelOrderUseCase.execute(id, request);

        // Only releaseReserved — no addStock, no StockMovement
        assertEquals(50, stock.getQuantity());  // unchanged
        assertEquals(5, stock.getReserved());   // 10 - 5
        verify(warehouseStockRepository).save(stock);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void should_RestoreStockAndCreateMovement_When_CancellingConfirmedOrder() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLineAllocation allocation = OrderLineAllocation.create(warehouseId, BigDecimal.valueOf(5));
        OrderLine line = OrderLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Order order = Order.reconstitute(
                OrderId.of(id), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.CONFIRMED, null, null, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
        CancelOrderRequest request = new CancelOrderRequest("Supplier issue");

        // After deductReserved(5): quantity went from 50→45, reserved went from 10→5
        // (5 remaining reserved belongs to OTHER orders, not this one)
        // On cancel, only addStock(5) restores quantity. releaseReserved is NOT called
        // because deductReserved already consumed this order's reservation.
        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 45, 5,
                LocalDateTime.now(), LocalDateTime.now());

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        cancelOrderUseCase.execute(id, request);

        // Restore stock only — releaseReserved is NOT called for confirmed orders
        assertEquals(50, stock.getQuantity());  // 45 + 5
        assertEquals(5, stock.getReserved());   // unchanged — reserved=5 belongs to other orders

        verify(stockMovementRepository).save(stockMovementCaptor.capture());
        StockMovement movement = stockMovementCaptor.getValue();
        assertEquals(MovementType.ENTRY, movement.getMovementType());
        assertEquals(5, movement.getQuantity());
        assertEquals(45, movement.getPreviousStock());
        assertEquals(50, movement.getNewStock());
        assertEquals("Order cancelled", movement.getNotes());
    }

    @Test
    void should_SaveExactlyOnce_When_CancellationSucceeds() {
        UUID id = UUID.randomUUID();
        Order order = buildOrder(id, OrderStatus.DRAFT);
        CancelOrderRequest request = new CancelOrderRequest("Reason");

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        cancelOrderUseCase.execute(id, request);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    private Order buildOrder(UUID orderId, OrderStatus status) {
        OrderLine line = OrderLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        return Order.reconstitute(
                OrderId.of(orderId), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                status, null, null, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
