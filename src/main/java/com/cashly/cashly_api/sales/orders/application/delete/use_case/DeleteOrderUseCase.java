package com.cashly.cashly_api.sales.orders.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderInventoryGateway inventory;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteOrderUseCase(OrderRepository orderRepository,
            OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.orderRepository = orderRepository;
        this.inventory = inventory;
    }

    @Transactional
    public void execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.delete();
        revertStockAllocations(order);

        activityNotifications.record(ActivityEvent.of("SALES_ORDERS",
                "DELETE", "ORDER", order.getId().value(),
                order.getOrderNumber().value(), null));
        orderRepository.deleteById(OrderId.of(id));
    }

    private void revertStockAllocations(Order order) {
        inventory.release(order.getLines().stream().flatMap(line -> line
                .getAllocations().stream()
                .map(allocation -> new OrderInventoryGateway.Allocation(
                        allocation.getWarehouseId(), line.getProductId(),
                        allocation.getAllocated().intValue())))
                .toList());
    }
}
