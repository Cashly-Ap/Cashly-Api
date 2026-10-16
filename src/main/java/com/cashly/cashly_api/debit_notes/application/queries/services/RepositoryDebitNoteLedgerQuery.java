package com.cashly.cashly_api.debit_notes.application.queries.services;
import com.cashly.cashly_api.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.debit_notes.domain.repository.DebitNoteRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public class RepositoryDebitNoteLedgerQuery implements DebitNoteLedgerQuery {
    private final DebitNoteRepository notes;
    public RepositoryDebitNoteLedgerQuery(DebitNoteRepository notes) { this.notes = notes; }
    public List<BigDecimal> amountsByInvoiceId(UUID invoiceId) {
        return notes.findByInvoiceId(invoiceId).stream().map(note -> note.getAmount()).toList();
    }
}
