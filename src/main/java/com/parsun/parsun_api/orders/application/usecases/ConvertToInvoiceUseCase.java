package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.ConvertToInvoiceResponse;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConvertToInvoiceUseCase {

    private final OrderRepository orderRepository;

    public ConvertToInvoiceUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ConvertToInvoiceResponse execute(UUID orderId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        UUID invoiceId = UUID.randomUUID();
        String invoiceNumber = "INV-STUB-" + invoiceId.toString().substring(0, 8).toUpperCase();

        order.markAsInvoiced(invoiceId, invoiceNumber);
        orderRepository.save(order);

        return new ConvertToInvoiceResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getStatus().name(),
                invoiceId,
                invoiceNumber
        );
    }
}
