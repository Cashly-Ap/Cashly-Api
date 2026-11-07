package com.cashly.cashly_api.sales.orders.application.confirm.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.sales.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.sales.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConfirmOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderInventoryGateway inventory;

    private final ActivityNotificationRecorder activityNotifications;

    public ConfirmOrderUseCase(OrderRepository orderRepository,
            OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.orderRepository = orderRepository;
        this.inventory = inventory;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.confirm();
        inventory.deductReservations(
                order.getLines().stream().flatMap(line -> line.getAllocations()
                        .stream()
                        .map(allocation -> new OrderInventoryGateway.Allocation(
                                allocation.getWarehouseId(),
                                line.getProductId(),
                                allocation.getAllocated().intValue())))
                        .toList(),
                new OrderInventoryGateway.Reference(order.getId().value(),
                        order.getOrderNumber().value(), "Order confirmed"));

        Order saved = orderRepository.save(order);
        activityNotifications.record(ActivityEvent.of("SALES_ORDERS",
                "CONFIRM", "ORDER", saved.getId().value(),
                saved.getOrderNumber().value(), null));
        return OrderMapper.toDetailResponse(saved);
    }

}
