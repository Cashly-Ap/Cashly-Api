package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.contracts.OrderActorGateway;
import com.cashly.cashly_api.orders.application.contracts.OrderCustomerGateway;
import com.cashly.cashly_api.orders.application.contracts.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.contracts.OrderProductCatalogGateway;
import com.cashly.cashly_api.orders.application.dto.request.CreateOrderRequest;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderCustomerGateway customers;
    private final OrderProductCatalogGateway products;
    private final OrderActorGateway actors;
    private final OrderInventoryGateway inventory;

    public CreateOrderUseCase(OrderRepository orderRepository,
                               OrderCustomerGateway customers,
                               OrderProductCatalogGateway products,
                               OrderActorGateway actors,
                               OrderInventoryGateway inventory) {
        this.orderRepository = orderRepository;
        this.customers = customers;
        this.products = products;
        this.actors = actors;
        this.inventory = inventory;
    }

    @Transactional
    public OrderDetailResponse execute(CreateOrderRequest request, UUID authenticatedUserId) {
        var customer = customers.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        var user = actors.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        List<OrderLine> lines = buildOrderLines(request);

        int year = LocalDate.now().getYear();
        int sequence = orderRepository.getNextSequenceForYear(year);
        OrderNumber orderNumber = OrderNumber.of(year, sequence);

        Order order = Order.create(
                orderNumber,
                customer.id(),
                customer.legalName(),
                customer.rif(),
                user.id(),
                user.name(),
                request.notes(),
                lines
        );

        Order saved = orderRepository.save(order);
        return OrderMapper.toDetailResponse(saved);
    }

    private List<OrderLine> buildOrderLines(CreateOrderRequest request) {
        return request.lines().stream()
                .map(lineRequest -> {
                    var product = products.findProduct(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                    List<OrderLineAllocation> allocations = applyStockAllocations(lineRequest, product.id());

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
    }

    private List<OrderLineAllocation> applyStockAllocations(CreateOrderRequest.OrderLineRequest lineRequest, UUID productId) {
        List<OrderLineAllocation> allocations = new java.util.ArrayList<>();
        for (CreateOrderRequest.WarehouseAllocationRequest allocation : lineRequest.warehouseAllocations()) {
            inventory.reserve(List.of(new OrderInventoryGateway.Allocation(allocation.warehouseId(), productId,
                    allocation.allocated())));

            allocations.add(OrderLineAllocation.create(allocation.warehouseId(), BigDecimal.valueOf(allocation.allocated())));
        }
        return allocations;
    }
}
