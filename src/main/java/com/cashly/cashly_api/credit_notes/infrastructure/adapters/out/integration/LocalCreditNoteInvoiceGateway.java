package com.cashly.cashly_api.credit_notes.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceNoteApi;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LocalCreditNoteInvoiceGateway implements CreditNoteInvoiceGateway {
    private final InvoiceNoteApi invoices;

    public LocalCreditNoteInvoiceGateway(InvoiceNoteApi invoices) {
        this.invoices = invoices;
    }

    @Override
    public CreditNoteInvoiceContext find(UUID invoiceId) {
        InvoiceNoteApi.Context context = invoices.find(invoiceId);
        return new CreditNoteInvoiceContext(context.id(), context.number(), context.status(),
                context.statusLabel(), context.customerName(), context.customerRif(), context.totalBeforeNotes());
    }

    @Override
    public Map<UUID, CreditNoteInvoiceContext> findAll(Set<UUID> invoiceIds) {
        return invoices.findAll(invoiceIds).entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new CreditNoteInvoiceContext(entry.getValue().id(), entry.getValue().number(),
                        entry.getValue().status(), entry.getValue().statusLabel(),
                        entry.getValue().customerName(), entry.getValue().customerRif(),
                        entry.getValue().totalBeforeNotes())));
    }

    @Override
    public void recalculate(UUID invoiceId) {
        invoices.recalculate(invoiceId);
    }
}
