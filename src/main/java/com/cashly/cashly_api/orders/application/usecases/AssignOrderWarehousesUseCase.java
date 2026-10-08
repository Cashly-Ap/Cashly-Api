package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.contracts.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.contracts.OrderWarehouseGateway;
import com.cashly.cashly_api.orders.application.dto.request.AssignOrderWarehousesRequest;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class AssignOrderWarehousesUseCase {

    private final OrderRepository orderRepository;
    private final OrderInventoryGateway inventory;
    private final OrderWarehouseGateway warehouses;

    public AssignOrderWarehousesUseCase(OrderRepository orderRepository,
                                         OrderInventoryGateway inventory,
                                         OrderWarehouseGateway warehouses) {
        this.orderRepository = orderRepository;
        this.inventory = inventory;
        this.warehouses = warehouses;
    }

    @Transactional
    public OrderDetailResponse execute(UUID orderId, AssignOrderWarehousesRequest request) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        UUID customerId = order.getCustomerId();

        for (var lineRequest : request.lines()) {
            OrderLine line = order.getLines().stream()
                    .filter(l -> l.getId().value().equals(lineRequest.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Order line not found: " + lineRequest.lineId()));

            BigDecimal lineAllocatedTotal = BigDecimal.ZERO;
            for (var allocationRequest : lineRequest.allocations()) {
                assertCustomerWarehouseAccess(customerId, allocationRequest.warehouseId());

                inventory.reserve(java.util.List.of(new OrderInventoryGateway.Allocation(
                        allocationRequest.warehouseId(), line.getProductId(),
                        allocationRequest.allocatedQuantity().intValue())));

                OrderLineAllocation allocation = OrderLineAllocation.create(
                        allocationRequest.warehouseId(),
                        allocationRequest.allocatedQuantity()
                );

                line.getAllocations().add(allocation);
                lineAllocatedTotal = lineAllocatedTotal.add(allocationRequest.allocatedQuantity());
            }

            if (lineAllocatedTotal.compareTo(line.getQuantity()) != 0) {
                throw new IllegalArgumentException(
                        "Allocated quantity must equal line quantity for line " + lineRequest.lineId());
            }
        }

        Order saved = orderRepository.save(order);
        return OrderMapper.toDetailResponse(saved);
    }

    private void assertCustomerWarehouseAccess(UUID customerId, UUID warehouseId) {
        boolean hasAccess = warehouses.hasCustomerAccess(customerId, warehouseId);
        if (!hasAccess) {
            throw new IllegalArgumentException(
                    "Customer " + customerId + " does not have access to warehouse " + warehouseId);
        }
    }
}
