package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.orders.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.orders.application.ports.out.CustomerCreditGateway;
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
@DisplayName("GetOrderByIdUseCase Unit Tests")
class GetOrderByIdUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StockAvailabilityGateway stockEnrichmentService;

    @Mock
    private CustomerCreditGateway creditGuard;

    private GetOrderByIdUseCase getOrderByIdUseCase;

    @BeforeEach
    void setUp() {
        getOrderByIdUseCase = new GetOrderByIdUseCase(orderRepository, stockEnrichmentService, creditGuard);
    }

    @Test
    void should_ReturnOrder_When_OrderExists() {
        UUID id = UUID.randomUUID();
        Order order = buildOrder(id);

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(stockEnrichmentService.enrich(anyList())).thenReturn(List.of());
        when(stockEnrichmentService.getAllStockByProducts(any(), anyList())).thenReturn(java.util.Map.of());

        OrderDetailResponse response = getOrderByIdUseCase.execute(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("DRAFT", response.status());
        verify(orderRepository).findById(OrderId.of(id));
    }

    @Test
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> getOrderByIdUseCase.execute(id)
        );

        assertEquals("Order not found with id: " + id, exception.getMessage());
    }

    @Test
    void should_EnrichAllocations_When_OrderHasAllocations() {
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
        when(stockEnrichmentService.enrich(anyList())).thenReturn(List.of());
        when(stockEnrichmentService.getAllStockByProducts(any(), anyList())).thenReturn(java.util.Map.of());

        OrderDetailResponse response = getOrderByIdUseCase.execute(id);

        assertNotNull(response);
        assertEquals(1, response.lines().size());
        verify(stockEnrichmentService).enrich(anyList());
        verify(stockEnrichmentService).getAllStockByProducts(any(), anyList());
    }

    private Order buildOrder(UUID orderId) {
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
                null,
                null,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
