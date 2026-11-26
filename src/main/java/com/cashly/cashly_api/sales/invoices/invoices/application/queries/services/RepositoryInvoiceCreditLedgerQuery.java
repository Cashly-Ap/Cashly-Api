package com.cashly.cashly_api.sales.invoices.invoices.application.queries.services;

import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.InvoiceCreditLedgerEntry;
import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.InvoiceCreditLedgerQuery;
import com.cashly.cashly_api.sales.invoices.invoices.domain.repository.InvoiceRepository;
import java.util.List;
import java.util.UUID;

public class RepositoryInvoiceCreditLedgerQuery
        implements
            InvoiceCreditLedgerQuery {

    private final InvoiceRepository invoices;

    public RepositoryInvoiceCreditLedgerQuery(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public List<InvoiceCreditLedgerEntry> findAll() {
        return entries(invoices.findAllHeaders());
    }

    public List<InvoiceCreditLedgerEntry> findByCustomerId(UUID customerId) {
        return entries(invoices.findHeaders(customerId, null));
    }

    private List<InvoiceCreditLedgerEntry> entries(
            List<InvoiceRepository.InvoiceHeader> headers) {
        return headers.stream()
                .map(header -> new InvoiceCreditLedgerEntry(header.id(),
                        header.number(), pendingPayment(header.status()),
                        header.subtotal(), header.returnedTotal(),
                        header.dueDate()))
                .toList();
    }

    private boolean pendingPayment(String status) {
        return "PENDING_PAYMENT".equals(status)
                || "PARTIALLY_PAID".equals(status);
    }
}
