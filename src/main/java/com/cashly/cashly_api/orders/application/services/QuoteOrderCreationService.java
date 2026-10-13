package com.cashly.cashly_api.orders.application.services;

import com.cashly.cashly_api.orders.application.ports.in.QuoteOrderCreationApi;
import com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class QuoteOrderCreationService implements QuoteOrderCreationApi {
    private final OrderRepository orders;
    private final OrderInventoryGateway inventory;

    public QuoteOrderCreationService(OrderRepository orders, OrderInventoryGateway inventory) {
        this.orders = orders;
        this.inventory = inventory;
    }

    @Override
    @Transactional
    public CreatedOrder create(Command command) {
        int year = LocalDate.now().getYear();
        var number = OrderNumber.of(year, orders.getNextSequenceForYear(year));
        var lines = command.lines().stream().map(line -> OrderLine.create(line.productId(), line.productName(),
                line.productCode(), line.categoryId(), line.quantity(), line.unitPrice(), line.allocations().stream()
                        .map(item -> OrderLineAllocation.create(item.warehouseId(), item.quantity())).toList())).toList();
        var order = Order.reconstitute(OrderId.generate(), number, command.customerId(), command.customerName(),
                command.customerRif(), command.actorId(), command.actorName(), OrderStatus.CONFIRMED, null,
                null, null, null, null, lines, LocalDateTime.now(), LocalDateTime.now());
        inventory.deduct(command.lines().stream().flatMap(line -> line.allocations().stream()
                .map(item -> new OrderInventoryGateway.Allocation(item.warehouseId(), item.productId(),
                        item.quantity().intValue()))).toList());
        var saved = orders.save(order);
        return new CreatedOrder(saved.getId().value(), saved.getOrderNumber().value());
    }
}
