package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConfirmOrderUseCase {

    private final OrderRepository orderRepository;

    public ConfirmOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.confirm();

        Order saved = orderRepository.save(order);
        return OrderMapper.toDetailResponse(saved);
    }
}
