package com.cashly.cashly_api.credit_notes.application.queries;
import com.cashly.cashly_api.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.credit_notes.domain.CreditNoteRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public class RepositoryCreditNoteLedgerQuery implements CreditNoteLedgerQuery {
    private final CreditNoteRepository notes;
    public RepositoryCreditNoteLedgerQuery(CreditNoteRepository notes) { this.notes = notes; }
    public List<BigDecimal> amountsByInvoiceId(UUID invoiceId) {
        return notes.findByInvoiceId(invoiceId).stream().map(note -> note.getAmount()).toList();
    }
}
