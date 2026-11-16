package com.cashly.cashly_api.sales.quotes.reporting.infrastructure.integration;

import com.cashly.cashly_api.sales.quotes.application.ports.in.QuoteDocumentQuery;
import com.cashly.cashly_api.sales.quotes.application.ports.in.QuoteDocumentFilter;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuoteReportQueryGateway;
import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportData;
import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportRow;
import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportFilter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class QuoteDocumentQueryAdapter implements QuoteReportQueryGateway {
    private final QuoteDocumentQuery query;
    public QuoteDocumentQueryAdapter(QuoteDocumentQuery query) {
        this.query = query;
    }
    public QuoteReportData find(UUID id) {
        var data = query.find(id);
        return new QuoteReportData(data.id(), data.quoteNumber(),
                data.customerName(), data.customerRif(), data.status(),
                data.notes(), data.cancelledReason(), data.createdAt(),
                data.subtotal(),
                data.lines().stream()
                        .map(line -> new QuoteReportData.Line(
                                line.productCode(), line.productName(),
                                line.quantity(), line.unitPrice(),
                                line.lineTotal()))
                        .toList());
    }
    public List<QuoteReportRow> findAll(QuoteReportFilter filter) {
        return query
                .findAll(new QuoteDocumentFilter(filter.dateFrom(),
                        filter.dateTo(), filter.customerId(), filter.status(),
                        filter.productId(), filter.categoryId()))
                .stream()
                .map(data -> new QuoteReportRow(data.id(), data.quoteNumber(),
                        data.customerName(), data.status(), data.subtotal(),
                        data.createdAt()))
                .toList();
    }
}