package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrdersUseCase Unit Tests")
class GetOrdersUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    private GetOrdersUseCase getOrdersUseCase;

    @BeforeEach
    void setUp() {
        getOrdersUseCase = new GetOrdersUseCase(orderRepository);
    }

    @Test
    void should_ReturnPaginatedList_When_OrdersExist() {
        UUID customerId = UUID.randomUUID();
        List<Order> orders = List.of(
                buildOrder(UUID.randomUUID(), customerId, OrderStatus.DRAFT),
                buildOrder(UUID.randomUUID(), customerId, OrderStatus.CONFIRMED)
        );

        when(orderRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(orders);

        List<OrderListResponse> result = getOrdersUseCase.execute(
                null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findAllWithFilters(null, null, null, null, null, null, null, null);
    }

    @Test
    void should_PassFiltersToRepository_When_FiltersProvided() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        UUID customerId = UUID.randomUUID();
        String status = "DRAFT";
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(orderRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        getOrdersUseCase.execute(dateFrom, dateTo, customerId, status, productId, categoryId, "createdAt", "desc");

        verify(orderRepository).findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId, "createdAt", "desc");
    }

    @Test
    void should_ReturnEmptyList_When_NoOrdersMatch() {
        when(orderRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<OrderListResponse> result = getOrdersUseCase.execute(
                null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void should_FilterByCustomerId_When_CustomerIdProvided() {
        UUID customerId = UUID.randomUUID();
        Order order = buildOrder(UUID.randomUUID(), customerId, OrderStatus.DRAFT);

        when(orderRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(order));

        List<OrderListResponse> result = getOrdersUseCase.execute(
                null, null, customerId, null, null, null, null, null);

        assertEquals(1, result.size());
        verify(orderRepository).findAllWithFilters(null, null, customerId, null, null, null, null, null);
    }

    @Test
    void should_FilterByStatus_When_StatusProvided() {
        when(orderRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        getOrdersUseCase.execute(null, null, null, "CONFIRMED", null, null, null, null);

        verify(orderRepository).findAllWithFilters(null, null, null, "CONFIRMED", null, null, null, null);
    }

    private Order buildOrder(UUID id, UUID customerId, OrderStatus status) {
        OrderLine line = OrderLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        return Order.reconstitute(
                OrderId.of(id),
                OrderNumber.of(2026, 1),
                customerId,
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator Name",
                status,
                null, null, null, null, null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
