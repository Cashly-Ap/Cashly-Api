package com.cashly.cashly_api.invoices.reporting.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.ports.in.InvoiceDocumentQuery;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceDocumentFilter;
import com.cashly.cashly_api.invoices.reporting.application.ports.out.InvoiceReportQueryGateway;
import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportData;
import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportFilter;
import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportRow;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class InvoiceDocumentQueryAdapter implements InvoiceReportQueryGateway {
    private final InvoiceDocumentQuery query;

    public InvoiceDocumentQueryAdapter(InvoiceDocumentQuery query) { this.query = query; }

    public InvoiceReportData find(UUID invoiceId) {
        var invoice = query.find(invoiceId);
        return new InvoiceReportData(invoice.id(), invoice.invoiceNumber(), invoice.customerName(), invoice.customerRif(), invoice.orderNumber(),
                invoice.status(), invoice.notes(), invoice.cancelledReason(), invoice.createdAt(), invoice.subtotal(), invoice.paymentType(), invoice.total(),
                invoice.lines().stream().map(line -> new InvoiceReportData.Line(line.productCode(), line.productName(), line.quantity(), line.unitPrice(), line.lineTotal())).toList());
    }

    public List<InvoiceReportRow> findAll(InvoiceReportFilter filter) {
        return query.findAll(new InvoiceDocumentFilter(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.statuses(), filter.productId(), filter.categoryId()))
                .stream().map(invoice -> new InvoiceReportRow(invoice.id(), invoice.invoiceNumber(), invoice.customerName(), invoice.paymentType(), invoice.lineCount(), invoice.status(), invoice.total(), invoice.createdAt())).toList();
    }
}
