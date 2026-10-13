package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderInventoryGateway inventory;

    public DeleteOrderUseCase(OrderRepository orderRepository,
                               OrderInventoryGateway inventory) {
        this.orderRepository = orderRepository;
        this.inventory = inventory;
    }

    @Transactional
    public void execute(UUID id) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.delete();
        revertStockAllocations(order);

        orderRepository.deleteById(OrderId.of(id));
    }

    private void revertStockAllocations(Order order) {
        inventory.release(order.getLines().stream().flatMap(line -> line.getAllocations().stream()
                .map(allocation -> new OrderInventoryGateway.Allocation(allocation.getWarehouseId(),
                        line.getProductId(), allocation.getAllocated().intValue()))).toList());
    }
}
