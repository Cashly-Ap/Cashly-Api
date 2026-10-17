package com.cashly.cashly_api.finance.billing.debit_notes.application.queries.services;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class RepositoryDebitNoteLedgerQueryTest {
    private final DebitNoteRepository notes = mock(DebitNoteRepository.class);
    private final RepositoryDebitNoteLedgerQuery query = new RepositoryDebitNoteLedgerQuery(notes);
    @Test
    void should_preserve_all_note_amounts_for_requested_invoice() {
        UUID id = UUID.randomUUID();
        var first = mock(DebitNote.class);
        var second = mock(DebitNote.class);
        when(first.getAmount()).thenReturn(new BigDecimal("12.50"));
        when(second.getAmount()).thenReturn(new BigDecimal("7.50"));
        when(notes.findByInvoiceId(id)).thenReturn(List.of(first, second));
        assertThat(query.amountsByInvoiceId(id)).containsExactly(new BigDecimal("12.50"), new BigDecimal("7.50"));
        verify(notes).findByInvoiceId(id);
    }
    @Test
    void should_return_empty_amounts_without_notes() {
        assertThat(query.amountsByInvoiceId(UUID.randomUUID())).isEmpty();
    }
}
