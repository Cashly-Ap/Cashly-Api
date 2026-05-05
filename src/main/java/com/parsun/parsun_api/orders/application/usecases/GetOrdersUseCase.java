package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetOrdersUseCase {

    private final OrderRepository orderRepository;

    public GetOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderListResponse> execute(UUID customerId, OrderStatus status,
                                           LocalDate fromDate, LocalDate toDate,
                                           UUID createdBy) {
        return orderRepository.findAllWithFilters(customerId, status, fromDate, toDate, createdBy)
                .stream()
                .map(OrderMapper::toListResponse)
                .toList();
    }
}
