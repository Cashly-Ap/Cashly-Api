package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public DeleteOrderUseCase(OrderRepository orderRepository,
                               WarehouseStockRepository warehouseStockRepository) {
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
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
        for (OrderLine line : order.getLines()) {
            for (OrderLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            stock.releaseReserved(allocation.getAllocated().intValue());
                            warehouseStockRepository.save(stock);
                        });
            }
        }
    }
}
