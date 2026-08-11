package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.orders.application.dto.request.CancelOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.ReferenceType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationService notificationService;

    public CancelOrderUseCase(OrderRepository orderRepository,
                               WarehouseStockRepository warehouseStockRepository,
                               StockMovementRepository stockMovementRepository,
                               NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id, CancelOrderRequest request) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus previousStatus = order.getStatus();
        order.cancel(request.reason());
        revertStockAllocations(order, previousStatus);

        Order saved = orderRepository.save(order);
        notificationService.notifyCustomerOrderRejected(order.getCustomerId(), saved.getId().value(), request.reason());
        return OrderMapper.toDetailResponse(saved);
    }

    private void revertStockAllocations(Order order, OrderStatus previousStatus) {
        boolean wasConfirmedOrInvoiced = previousStatus == OrderStatus.CONFIRMED
                || previousStatus == OrderStatus.INVOICED;

        for (OrderLine line : order.getLines()) {
            for (OrderLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            if (wasConfirmedOrInvoiced) {
                                int previous = stock.getQuantity();
                                stock.addStock(allocation.getAllocated().intValue());
                                
                                
                                
                                warehouseStockRepository.save(stock);

                                stockMovementRepository.save(StockMovement.createWithReference(
                                        allocation.getWarehouseId(),
                                        line.getProductId(),
                                        MovementType.CANCEL_ENTRY,
                                        allocation.getAllocated().intValue(),
                                        previous,
                                        stock.getQuantity(),
                                        null,
                                        null,
                                        ReferenceType.ORDER,
                                        order.getId().value(),
                                        order.getOrderNumber().value(),
                                        "Order cancelled"
                                ));
                            } else {
                                stock.releaseReserved(allocation.getAllocated().intValue());
                                warehouseStockRepository.save(stock);
                            }
                        });
            }
        }
    }
}
