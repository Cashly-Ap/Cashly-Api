package com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceNoteApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LocalDebitNoteInvoiceGateway implements DebitNoteInvoiceGateway {
    private final InvoiceNoteApi invoices;

    public LocalDebitNoteInvoiceGateway(InvoiceNoteApi invoices) {
        this.invoices = invoices;
    }

    @Override
    public InvoiceContext find(UUID invoiceId) {
        return map(invoices.find(invoiceId));
    }

    @Override
    public Map<UUID, InvoiceContext> findAll(Set<UUID> invoiceIds) {
        return invoices.findAll(invoiceIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> map(entry.getValue())));
    }

    @Override
    public void recalculate(UUID invoiceId) {
        invoices.recalculate(invoiceId);
    }

    private InvoiceContext map(InvoiceNoteApi.Context context) {
        return new InvoiceContext(context.id(), context.number(),
                context.status(), context.statusLabel(), context.customerName(),
                context.customerRif());
    }
}
