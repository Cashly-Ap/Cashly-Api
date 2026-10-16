package com.cashly.cashly_api.invoices.application.queries.services;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceCreditLedgerQuery;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceCreditLedgerEntry;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import java.util.List;
import java.util.UUID;
public class RepositoryInvoiceCreditLedgerQuery implements InvoiceCreditLedgerQuery {
    private final InvoiceRepository invoices;
    public RepositoryInvoiceCreditLedgerQuery(InvoiceRepository invoices) { this.invoices = invoices; }
    public List<InvoiceCreditLedgerEntry> findAll() { return findByCustomerId(null); }
    public List<InvoiceCreditLedgerEntry> findByCustomerId(UUID customerId) {
        return invoices.findAllWithFilters(null, null, customerId, null, null, null, null, "createdAt", "desc")
                .stream().map(invoice -> new InvoiceCreditLedgerEntry(invoice.getId().value(), invoice.getInvoiceNumber().value(),
                        invoice.getStatus() == InvoiceStatus.PENDING_PAYMENT || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID,
                        invoice.getSubtotal(), invoice.getReturnedTotal(), invoice.getDueDate())).toList();
    }
}
