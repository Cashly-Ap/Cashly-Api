package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.request.UpdateOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public class UpdateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public UpdateOrderUseCase(OrderRepository orderRepository,
                               ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(OrderId.of(id))
                .orElseThrow(() -> new OrderNotFoundException(id));

        List<OrderLine> lines = request.lines().stream()
                .map(lineRequest -> {
                    Product product = productRepository.findById(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));
                    return OrderLine.create(
                            product.getId(),
                            product.getName(),
                            product.getCode(),
                            lineRequest.quantity(),
                            lineRequest.unitPrice()
                    );
                })
                .toList();

        order.update(request.deliveryDate(), request.notes(), lines);

        Order saved = orderRepository.save(order);
        return OrderMapper.toDetailResponse(saved);
    }
}
