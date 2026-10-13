package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.dto.request.UpdateOrderRequest;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class UpdateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderProductCatalogGateway products;
    private final OrderInventoryGateway inventory;

    public UpdateOrderUseCase(OrderRepository orderRepository,
                               OrderProductCatalogGateway products,
                               OrderInventoryGateway inventory) {
        this.orderRepository = orderRepository;
        this.products = products;
        this.inventory = inventory;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT orders can be updated");
        }

        revertStockAllocations(order);

        List<OrderLine> lines = request.lines().stream()
                .map(lineRequest -> {
                    var product = products.findProduct(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                    List<OrderLineAllocation> allocations = applyStockAllocations(
                            lineRequest, product.id());

                    return OrderLine.create(
                            product.id(),
                            product.name(),
                            product.code(),
                            product.categoryId(),
                            lineRequest.quantity(),
                            lineRequest.unitPrice(),
                            allocations
                    );
                })
                .toList();

        order.update(request.notes(), lines);

        Order saved = orderRepository.save(order);
        return OrderMapper.toDetailResponse(saved);
    }

    private void revertStockAllocations(Order order) {
        inventory.release(order.getLines().stream().flatMap(line -> line.getAllocations().stream()
                .map(allocation -> new OrderInventoryGateway.Allocation(allocation.getWarehouseId(),
                        line.getProductId(), allocation.getAllocated().intValue()))).toList());
    }

    private List<OrderLineAllocation> applyStockAllocations(
            UpdateOrderRequest.OrderLineRequest lineRequest, UUID productId) {

        List<OrderLineAllocation> allocations = new java.util.ArrayList<>();
        for (UpdateOrderRequest.WarehouseAllocationRequest allocation : lineRequest.warehouseAllocations()) {
            inventory.reserve(List.of(new OrderInventoryGateway.Allocation(allocation.warehouseId(), productId,
                    allocation.allocated().intValue())));

            allocations.add(OrderLineAllocation.create(allocation.warehouseId(), allocation.allocated()));
        }
        return allocations;
    }
}
