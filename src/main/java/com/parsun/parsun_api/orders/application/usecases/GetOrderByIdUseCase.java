package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;

import java.util.UUID;

public class GetOrderByIdUseCase {

    private final OrderRepository orderRepository;

    public GetOrderByIdUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDetailResponse execute(UUID id) {
        return orderRepository.findById(OrderId.of(id))
                .map(OrderMapper::toDetailResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
