package com.cashly.cashly_api.finance.billing.debit_notes.application.queries.services;

import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.in.InvoiceDebitNoteDetailsQuery;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InvoiceDebitNoteDetailsQueryService
        implements
            InvoiceDebitNoteDetailsQuery {
    private final DebitNoteRepository notes;

    public InvoiceDebitNoteDetailsQueryService(DebitNoteRepository notes) {
        this.notes = notes;
    }

    @Override
    public List<Note> findByInvoiceId(UUID invoiceId) {
        return notes.findByInvoiceId(invoiceId).stream()
                .map(note -> new Note(note.getId().value(),
                        note.getNoteNumber().value(), note.getAmount(),
                        note.getReason(), note.getCreatedAt()))
                .toList();
    }

    @Override
    public Map<UUID, java.math.BigDecimal> sumByInvoiceIds(
            List<UUID> invoiceIds) {
        return invoiceIds.isEmpty()
                ? Map.of()
                : notes.sumByInvoiceIds(invoiceIds);
    }
}
