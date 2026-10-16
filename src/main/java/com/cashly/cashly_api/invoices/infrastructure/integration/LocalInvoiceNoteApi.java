package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.ports.in.InvoiceNoteApi;
import com.cashly.cashly_api.invoices.domain.Invoice;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.InvoiceStatusService;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LocalInvoiceNoteApi implements InvoiceNoteApi {
    private final InvoiceRepository invoices;
    private final InvoiceStatusService statuses;

    public LocalInvoiceNoteApi(InvoiceRepository invoices, InvoiceStatusService statuses) {
        this.invoices = invoices;
        this.statuses = statuses;
    }

    @Override
    public Context find(UUID invoiceId) {
        Invoice invoice = invoices.findById(InvoiceId.of(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        BigDecimal totalBeforeNotes = invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO);
        return toContext(invoice, totalBeforeNotes);
    }

    @Override
    public Map<UUID, Context> findAll(Set<UUID> invoiceIds) {
        if (invoiceIds.isEmpty()) {
            return Map.of();
        }
        List<InvoiceId> ids = invoiceIds.stream().map(InvoiceId::of).toList();
        return invoices.findAllById(ids).stream().collect(Collectors.toMap(
                invoice -> invoice.getId().value(),
                invoice -> toContext(invoice, invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO))));
    }

    @Override
    public void recalculate(UUID invoiceId) {
        statuses.recalculate(invoiceId);
    }

    private Context toContext(Invoice invoice, BigDecimal totalBeforeNotes) {
        return new Context(invoice.getId().value(), invoice.getInvoiceNumber().value(),
                invoice.getStatus().name(), invoice.getStatus().label(), invoice.getCustomerName(),
                invoice.getCustomerRif(), totalBeforeNotes);
    }
}
