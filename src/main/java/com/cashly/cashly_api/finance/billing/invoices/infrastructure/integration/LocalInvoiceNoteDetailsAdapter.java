package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in.InvoiceCreditNoteDetailsQuery;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.in.InvoiceDebitNoteDetailsQuery;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNoteDetailsGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class LocalInvoiceNoteDetailsAdapter
        implements
            InvoiceNoteDetailsGateway {
    private final InvoiceCreditNoteDetailsQuery credits;
    private final InvoiceDebitNoteDetailsQuery debits;

    public LocalInvoiceNoteDetailsAdapter(InvoiceCreditNoteDetailsQuery credits,
            InvoiceDebitNoteDetailsQuery debits) {
        this.credits = credits;
        this.debits = debits;
    }

    @Override
    public List<Note> findCreditNotes(UUID invoiceId) {
        return credits.findByInvoiceId(invoiceId).stream()
                .map(note -> new Note(note.id(), note.number(), note.amount(),
                        note.reason(), note.createdAt()))
                .toList();
    }

    @Override
    public List<Note> findDebitNotes(UUID invoiceId) {
        return debits.findByInvoiceId(invoiceId).stream()
                .map(note -> new Note(note.id(), note.number(), note.amount(),
                        note.reason(), note.createdAt()))
                .toList();
    }

    @Override
    public Map<UUID, BigDecimal> sumCreditNotes(List<UUID> invoiceIds) {
        return credits.sumByInvoiceIds(invoiceIds);
    }

    @Override
    public Map<UUID, BigDecimal> sumDebitNotes(List<UUID> invoiceIds) {
        return debits.sumByInvoiceIds(invoiceIds);
    }
}
