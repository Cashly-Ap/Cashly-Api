package com.cashly.cashly_api.sales.invoices.invoices.application.queries.services;

import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.InvoiceDocumentFilter;
import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.InvoiceDocumentQuery;
import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.InvoiceDocumentRow;
import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.InvoiceDocumentSnapshot;
import com.cashly.cashly_api.sales.invoices.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.domain.repository.InvoiceRepository;
import java.util.List;
import java.util.UUID;

public class InvoiceDocumentQueryService implements InvoiceDocumentQuery {
    private final GetInvoiceByIdUseCase invoiceQuery;
    private final InvoiceRepository invoices;

    public InvoiceDocumentQueryService(GetInvoiceByIdUseCase invoiceQuery,
            InvoiceRepository invoices) {
        this.invoiceQuery = invoiceQuery;
        this.invoices = invoices;
    }

    public InvoiceDocumentSnapshot find(UUID invoiceId) {
        var invoice = invoiceQuery.execute(invoiceId);
        return new InvoiceDocumentSnapshot(invoice.id(),
                invoice.invoiceNumber(), invoice.customerName(),
                invoice.customerRif(), invoice.orderNumber(), invoice.status(),
                invoice.notes(), invoice.cancelledReason(), invoice.createdAt(),
                invoice.subtotal(), invoice.paymentType(), invoice.total(),
                invoice.lines().stream()
                        .map(line -> new InvoiceDocumentSnapshot.Line(
                                line.productCode(), line.productName(),
                                line.quantity(), line.unitPrice(),
                                line.lineTotal()))
                        .toList());
    }

    public List<InvoiceDocumentRow> findAll(InvoiceDocumentFilter filter) {
        return invoices.findReportRows(filter.dateFrom(), filter.dateTo(),
                filter.customerId(), filter.statuses(), filter.productId(),
                filter.categoryId()).stream()
                .map(row -> new InvoiceDocumentRow(row.id(), row.number(),
                        row.customerName(), row.paymentType(),
                        (int) row.lineCount(), row.status(), row.total(),
                        row.createdAt()))
                .toList();
    }
}
