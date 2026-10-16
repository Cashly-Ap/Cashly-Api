package com.cashly.cashly_api.orders.reporting.infrastructure.integration;

import com.cashly.cashly_api.orders.application.ports.in.OrderDocumentQuery;
import com.cashly.cashly_api.orders.application.ports.in.OrderDocumentFilter;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrderReportQueryGateway;
import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportData;
import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportRow;
import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportFilter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class OrderDocumentQueryAdapter implements OrderReportQueryGateway {
    private final OrderDocumentQuery query;
    public OrderDocumentQueryAdapter(OrderDocumentQuery query) { this.query = query; }
    public OrderReportData find(UUID id) {
        var data = query.find(id);
        return new OrderReportData(data.id(), data.orderNumber(), data.customerName(), data.customerRif(), data.status(),
                data.notes(), data.cancelledReason(), data.createdAt(), data.subtotal(),
                data.lines().stream().map(line -> new OrderReportData.Line(line.productCode(), line.productName(), line.quantity(), line.unitPrice(), line.lineTotal())).toList());
    }
    public List<OrderReportRow> findAll(OrderReportFilter filter) {
        return query.findAll(new OrderDocumentFilter(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.status(), filter.productId(), filter.categoryId()))
                .stream().map(data -> new OrderReportRow(data.id(), data.orderNumber(), data.customerName(), data.status(), data.subtotal(), data.createdAt(), data.lineCount(), data.invoiceId())).toList();
    }
}