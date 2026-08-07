package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.orders.application.dto.request.AssignOrderWarehousesRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class AssignOrderWarehousesUseCase {

    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final CustomerWarehouseAccessRepository customerWarehouseAccessRepository;

    public AssignOrderWarehousesUseCase(OrderRepository orderRepository,
                                         WarehouseStockRepository warehouseStockRepository,
                                         CustomerWarehouseAccessRepository customerWarehouseAccessRepository) {
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.customerWarehouseAccessRepository = customerWarehouseAccessRepository;
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

                WarehouseStock stock = warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocationRequest.warehouseId(), line.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Stock not found for warehouse=" + allocationRequest.warehouseId() + " product=" + line.getProductId()));

                stock.reserve(allocationRequest.allocatedQuantity().intValue());
                warehouseStockRepository.save(stock);

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
        boolean hasAccess = customerWarehouseAccessRepository
                .existsByCustomerIdAndWarehouseId(customerId, warehouseId);
        if (!hasAccess) {
            throw new IllegalArgumentException(
                    "Customer " + customerId + " does not have access to warehouse " + warehouseId);
        }
    }
}
