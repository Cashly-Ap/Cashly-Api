package com.cashly.cashly_api.sales.orders.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.sales.orders.application.update.request.UpdateOrderRequest;
import com.cashly.cashly_api.sales.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.sales.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLineAllocation;
import com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class UpdateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderProductCatalogGateway products;
    private final OrderInventoryGateway inventory;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateOrderUseCase(OrderRepository orderRepository,
            OrderProductCatalogGateway products,
            OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.orderRepository = orderRepository;
        this.products = products;
        this.inventory = inventory;
    }

    @Transactional
    @Audited(module = "sales", entityType = "ORDER", action = "EDIT")
    public OrderDetailResponse execute(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT orders can be updated");
        }

        revertStockAllocations(order);

        List<OrderLine> lines = request.lines().stream().map(lineRequest -> {
            var product = products.findProduct(lineRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found: " + lineRequest.productId()));

            List<OrderLineAllocation> allocations = applyStockAllocations(
                    lineRequest, product.id());

            return OrderLine.create(product.id(), product.name(),
                    product.code(), product.categoryId(),
                    lineRequest.quantity(), lineRequest.unitPrice(),
                    allocations);
        }).toList();

        order.update(request.notes(), lines);

        Order saved = orderRepository.save(order);
        activityNotifications.record(ActivityEvent.of("SALES_ORDERS",
                "EDIT", "ORDER", saved.getId().value(),
                saved.getOrderNumber().value(), null));
        return OrderMapper.toDetailResponse(saved);
    }

    private void revertStockAllocations(Order order) {
        inventory.release(order.getLines().stream().flatMap(line -> line
                .getAllocations().stream()
                .map(allocation -> new OrderInventoryGateway.Allocation(
                        allocation.getWarehouseId(), line.getProductId(),
                        allocation.getAllocated().intValue())))
                .toList());
    }

    private List<OrderLineAllocation> applyStockAllocations(
            UpdateOrderRequest.OrderLineRequest lineRequest, UUID productId) {

        List<OrderLineAllocation> allocations = new java.util.ArrayList<>();
        for (UpdateOrderRequest.WarehouseAllocationRequest allocation : lineRequest
                .warehouseAllocations()) {
            inventory.reserve(List.of(new OrderInventoryGateway.Allocation(
                    allocation.warehouseId(), productId,
                    allocation.allocated().intValue())));

            allocations.add(OrderLineAllocation.create(allocation.warehouseId(),
                    allocation.allocated()));
        }
        return allocations;
    }
}
