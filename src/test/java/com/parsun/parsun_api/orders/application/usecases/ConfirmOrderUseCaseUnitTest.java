package com.parsun.parsun_api.orders.application.usecases;

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
@DisplayName("ConfirmOrderUseCase Unit Tests")
class ConfirmOrderUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Captor
    private ArgumentCaptor<StockMovement> stockMovementCaptor;

    private ConfirmOrderUseCase confirmOrderUseCase;

    @BeforeEach
    void setUp() {
        confirmOrderUseCase = new ConfirmOrderUseCase(orderRepository,
                warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_ConfirmDraftOrder_When_OrderExists() {
        UUID id = UUID.randomUUID();
        Order order = buildDraftOrder(id);

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailResponse response = confirmOrderUseCase.execute(id);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.status());
        verify(orderRepository).findById(OrderId.of(id));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> confirmOrderUseCase.execute(id)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_ThrowIllegalStateException_When_OrderIsNotDraft() {
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
                IllegalStateException.class,
                () -> confirmOrderUseCase.execute(id)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_DeductReservedStock_When_ConfirmingOrder() {
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
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        confirmOrderUseCase.execute(id);

        assertEquals(45, stock.getQuantity());  // 50 - 5
        assertEquals(5, stock.getReserved());    // 10 - 5
        verify(warehouseStockRepository).save(stock);
    }

    @Test
    void should_CreateStockMovement_When_DeductingReservedStock() {
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
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        confirmOrderUseCase.execute(id);

        verify(stockMovementRepository).save(stockMovementCaptor.capture());
        StockMovement movement = stockMovementCaptor.getValue();

        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.EXIT, movement.getMovementType());
        assertEquals(5, movement.getQuantity());
        assertEquals(50, movement.getPreviousStock());
        assertEquals(45, movement.getNewStock());
        assertEquals("Pedido confirmado", movement.getNotes());
    }

    @Test
    void should_SaveExactlyOnce_When_ConfirmationSucceeds() {
        UUID id = UUID.randomUUID();
        Order order = buildDraftOrder(id);

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        confirmOrderUseCase.execute(id);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    private Order buildDraftOrder(UUID orderId) {
        OrderLine line = OrderLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        return Order.reconstitute(
                OrderId.of(orderId),
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
    }
}
