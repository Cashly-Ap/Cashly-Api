package com.cashly.cashly_api.orders.application.queries;

import com.cashly.cashly_api.orders.application.ports.in.OrderDocumentQuery;
import com.cashly.cashly_api.orders.application.ports.in.OrderDocumentSnapshot;
import com.cashly.cashly_api.orders.application.ports.in.OrderDocumentRow;
import com.cashly.cashly_api.orders.application.ports.in.OrderDocumentFilter;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.application.list.GetOrdersUseCase;
import com.cashly.cashly_api.orders.domain.OrderRepository;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import java.util.List;
import java.util.UUID;

public class OrderDocumentQueryService implements OrderDocumentQuery {
    private final OrderRepository repository;
    private final GetOrdersUseCase listQuery;
    public OrderDocumentQueryService(OrderRepository repository, GetOrdersUseCase listQuery) {
        this.repository = repository;
        this.listQuery = listQuery;
    }
    public OrderDocumentSnapshot find(UUID id) {
        var entity = repository.findById(OrderId.of(id)).orElseThrow(() -> new OrderNotFoundException(id));
        var data = OrderMapper.toDetailResponse(entity);
        return new OrderDocumentSnapshot(data.id(), data.orderNumber(), data.customerName(), data.customerRif(), data.status(),
                data.notes(), data.cancelledReason(), data.createdAt(), data.subtotal(),
                data.lines().stream().map(line -> new OrderDocumentSnapshot.Line(line.productCode(), line.productName(), line.quantity(), line.unitPrice(), line.lineTotal())).toList());
    }
    public List<OrderDocumentRow> findAll(OrderDocumentFilter filter) {
        return listQuery.execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc")
                .stream().map(data -> new OrderDocumentRow(data.id(), data.orderNumber(), data.customerName(), data.status(), data.subtotal(), data.createdAt(), data.lineCount(), data.invoiceId())).toList();
    }
}