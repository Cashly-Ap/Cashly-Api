package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.contracts.InvoiceOrderGateway;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalInvoiceOrderGateway implements InvoiceOrderGateway {
    private final OrderRepository orders;

    public LocalInvoiceOrderGateway(OrderRepository orders) {
        this.orders = orders;
    }

    @Override
    public Order findOrder(UUID orderId) {
        return orders.findById(OrderId.of(orderId)).map(order -> new Order(
                order.getId().value(), order.getOrderNumber().value(), order.getCustomerId(),
                order.getCustomerName(), order.getCustomerRif(), order.getStatus().name(),
                order.getLines().stream().map(line -> new Line(line.getProductId(), line.getProductName(),
                        line.getProductCode(), line.getQuantity(), line.getUnitPrice(),
                        line.getAllocations().stream().map(allocation -> new Allocation(
                                allocation.getWarehouseId(), allocation.getAllocated())).toList())).toList()))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    public void revertInvoiceLink(UUID orderId) {
        var order = orders.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.revertFromInvoiced();
        orders.save(order);
    }
}
