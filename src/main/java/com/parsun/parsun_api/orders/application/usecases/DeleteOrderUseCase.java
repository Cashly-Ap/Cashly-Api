package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;

    public DeleteOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.delete();

        orderRepository.deleteById(OrderId.of(id));
    }
}
