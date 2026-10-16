package com.cashly.cashly_api.orders.application.cancel.use_case;

import com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway;
import com.cashly.cashly_api.orders.application.cancel.request.CancelOrderRequest;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.entity.Order;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderInventoryGateway inventory;
    private final OrderNotificationGateway notifications;

    public CancelOrderUseCase(OrderRepository orderRepository,
                               OrderInventoryGateway inventory,
                               OrderNotificationGateway notifications) {
        this.orderRepository = orderRepository;
        this.inventory = inventory;
        this.notifications = notifications;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id, CancelOrderRequest request) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus previousStatus = order.getStatus();
        order.cancel(request.reason());
        revertStockAllocations(order, previousStatus);

        Order saved = orderRepository.save(order);
        notifications.notifyCustomerOrderRejected(order.getCustomerId(), saved.getId().value(), request.reason());
        return OrderMapper.toDetailResponse(saved);
    }

    private void revertStockAllocations(Order order, OrderStatus previousStatus) {
        boolean wasConfirmedOrInvoiced = previousStatus == OrderStatus.CONFIRMED
                || previousStatus == OrderStatus.INVOICED;

        var allocations = order.getLines().stream().flatMap(line -> line.getAllocations().stream()
                .map(allocation -> new OrderInventoryGateway.Allocation(allocation.getWarehouseId(),
                        line.getProductId(), allocation.getAllocated().intValue()))).toList();
        if (wasConfirmedOrInvoiced) {
            inventory.restore(allocations, new OrderInventoryGateway.Reference(order.getId().value(),
                    order.getOrderNumber().value(), "Order cancelled"));
        } else {
            inventory.release(allocations);
        }
    }
}
