package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrdersUseCase Unit Tests")
class GetOrdersUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    private GetOrdersUseCase getOrdersUseCase;

    @BeforeEach
    void setUp() {
        getOrdersUseCase = new GetOrdersUseCase(orderRepository);
    }

    @Test
    void should_ReturnOrders_When_FiltersAreApplied() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID customerId = UUID.randomUUID();
        String status = "DRAFT";
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Order order = buildOrder();
        when(orderRepository.findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId))
                .thenReturn(List.of(order));

        List<OrderListResponse> responses = getOrdersUseCase.execute(dateFrom, dateTo, customerId, status, productId, categoryId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(order.getId().value(), responses.getFirst().id());
        verify(orderRepository).findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId);
    }

    @Test
    void should_ReturnEmptyList_When_NoOrdersMatchFilters() {
        when(orderRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<OrderListResponse> responses = getOrdersUseCase.execute(
                LocalDate.now(), LocalDate.now(), null, null, null, null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void should_AcceptNullFilters() {
        Order order = buildOrder();
        when(orderRepository.findAllWithFilters(null, null, null, null, null, null))
                .thenReturn(List.of(order));

        List<OrderListResponse> responses = getOrdersUseCase.execute(null, null, null, null, null, null);

        assertEquals(1, responses.size());
        verify(orderRepository).findAllWithFilters(null, null, null, null, null, null);
    }

    private Order buildOrder() {
        OrderLine line = OrderLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        return Order.reconstitute(
                OrderId.of(UUID.randomUUID()),
                OrderNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator Name",
                OrderStatus.DRAFT,
                "Some notes",
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
