package com.cashly.cashly_api.orders.application.confirm;

import com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.Order;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConfirmOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderInventoryGateway inventory;
    private final OrderNotificationGateway notifications;

    public ConfirmOrderUseCase(OrderRepository orderRepository,
                                OrderInventoryGateway inventory,
                                OrderNotificationGateway notifications) {
        this.orderRepository = orderRepository;
        this.inventory = inventory;
        this.notifications = notifications;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.confirm();
        inventory.deductReservations(order.getLines().stream().flatMap(line -> line.getAllocations().stream()
                .map(allocation -> new OrderInventoryGateway.Allocation(allocation.getWarehouseId(),
                        line.getProductId(), allocation.getAllocated().intValue()))).toList(),
                new OrderInventoryGateway.Reference(order.getId().value(), order.getOrderNumber().value(), "Order confirmed"));

        Order saved = orderRepository.save(order);
        notifications.notifyCustomerOrderApproved(order.getCustomerId(), saved.getId().value());
        return OrderMapper.toDetailResponse(saved);
    }

}
