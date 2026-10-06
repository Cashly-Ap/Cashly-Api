package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetOrdersUseCase {

    private final OrderRepository orderRepository;

    public GetOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                           UUID customerId, String status,
                                           UUID productId, UUID categoryId,
                                           String orderBy, String orderDir) {
        return orderRepository.findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId, orderBy, orderDir)
                .stream()
                .map(OrderMapper::toListResponse)
                .toList();
    }
}
