package com.cashly.cashly_api.invoices.application.queries.services;

import com.cashly.cashly_api.invoices.application.ports.in.InvoiceDocumentFilter;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceDocumentQuery;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceDocumentRow;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceDocumentSnapshot;
import com.cashly.cashly_api.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.invoices.application.list.use_case.GetInvoicesUseCase;
import java.util.List;
import java.util.UUID;

public class InvoiceDocumentQueryService implements InvoiceDocumentQuery {
    private final GetInvoiceByIdUseCase invoiceQuery;
    private final GetInvoicesUseCase invoicesQuery;

    public InvoiceDocumentQueryService(GetInvoiceByIdUseCase invoiceQuery, GetInvoicesUseCase invoicesQuery) {
        this.invoiceQuery = invoiceQuery;
        this.invoicesQuery = invoicesQuery;
    }

    public InvoiceDocumentSnapshot find(UUID invoiceId) {
        var invoice = invoiceQuery.execute(invoiceId);
        return new InvoiceDocumentSnapshot(invoice.id(), invoice.invoiceNumber(), invoice.customerName(), invoice.customerRif(), invoice.orderNumber(),
                invoice.status(), invoice.notes(), invoice.cancelledReason(), invoice.createdAt(), invoice.subtotal(), invoice.paymentType(), invoice.total(),
                invoice.lines().stream().map(line -> new InvoiceDocumentSnapshot.Line(line.productCode(), line.productName(), line.quantity(), line.unitPrice(), line.lineTotal())).toList());
    }

    public List<InvoiceDocumentRow> findAll(InvoiceDocumentFilter filter) {
        return invoicesQuery.execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.statuses(), null, filter.productId(), filter.categoryId(), "createdAt", "desc")
                .stream().map(invoice -> new InvoiceDocumentRow(invoice.id(), invoice.invoiceNumber(), invoice.customerName(), invoice.paymentType(), invoice.lineCount(), invoice.status(), invoice.total(), invoice.createdAt())).toList();
    }
}
