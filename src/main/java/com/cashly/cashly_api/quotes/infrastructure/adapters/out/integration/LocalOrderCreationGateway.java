package com.cashly.cashly_api.quotes.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.orders.application.ports.in.QuoteOrderCreationApi;
import com.cashly.cashly_api.quotes.application.ports.out.OrderCreationGateway;
import org.springframework.stereotype.Component;

@Component
public class LocalOrderCreationGateway implements OrderCreationGateway {
    private final QuoteOrderCreationApi api;

    public LocalOrderCreationGateway(QuoteOrderCreationApi api) {
        this.api = api;
    }

    @Override
    public CreatedOrder create(Command command) {
        var lines = command.lines().stream().map(line -> new QuoteOrderCreationApi.Line(line.productId(),
                line.productName(), line.productCode(), line.categoryId(), line.quantity(), line.unitPrice(), line.allocations().stream()
                        .map(item -> new QuoteOrderCreationApi.Allocation(item.warehouseId(), item.productId(), item.quantity())).toList())).toList();
        var order = api.create(new QuoteOrderCreationApi.Command(command.customerId(), command.customerName(),
                command.customerRif(), command.actorId(), command.actorName(), lines));
        return new CreatedOrder(order.id(), order.number());
    }
}
