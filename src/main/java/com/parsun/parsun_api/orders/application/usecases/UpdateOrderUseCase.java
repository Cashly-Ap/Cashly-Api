package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.request.UpdateOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class UpdateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public UpdateOrderUseCase(OrderRepository orderRepository,
                               ProductRepository productRepository,
                               WarehouseStockRepository warehouseStockRepository,
                               StockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        revertStockAllocations(order);

        List<OrderLine> lines = request.lines().stream()
                .map(lineRequest -> {
                    Product product = productRepository.findById(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                    List<OrderLineAllocation> allocations = applyStockAllocations(
                            lineRequest, product.getId());

                    return OrderLine.create(
                            product.getId(),
                            product.getName(),
                            product.getCode(),
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
        for (OrderLine line : order.getLines()) {
            for (OrderLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            int previous = stock.getQuantity();
                            stock.addStock(allocation.getAllocated().intValue());
                            warehouseStockRepository.save(stock);

                            stockMovementRepository.save(StockMovement.create(
                                    allocation.getWarehouseId(),
                                    line.getProductId(),
                                    MovementType.ENTRY,
                                    allocation.getAllocated().intValue(),
                                    previous,
                                    stock.getQuantity(),
                                    null,
                                    null,
                                    "Order adjustment"
                            ));
                        });
            }
        }
    }

    private List<OrderLineAllocation> applyStockAllocations(
            UpdateOrderRequest.OrderLineRequest lineRequest, UUID productId) {

        List<OrderLineAllocation> allocations = new java.util.ArrayList<>();
        for (UpdateOrderRequest.WarehouseAllocationRequest allocation : lineRequest.warehouseAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.warehouseId(), productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock not found for warehouse=" + allocation.warehouseId() + " product=" + productId));

            int previous = stock.getQuantity();
            stock.removeStock(allocation.allocated().intValue());
            warehouseStockRepository.save(stock);

            stockMovementRepository.save(StockMovement.create(
                    allocation.warehouseId(),
                    productId,
                    MovementType.EXIT,
                    allocation.allocated().intValue(),
                    previous,
                    stock.getQuantity(),
                    null,
                    null,
                    "Order"
            ));

            allocations.add(OrderLineAllocation.create(allocation.warehouseId(), allocation.allocated()));
        }
        return allocations;
    }
}
