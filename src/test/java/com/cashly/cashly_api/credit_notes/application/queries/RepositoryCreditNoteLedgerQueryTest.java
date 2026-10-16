package com.cashly.cashly_api.credit_notes.application.queries;
import com.cashly.cashly_api.credit_notes.domain.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.CreditNoteRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class RepositoryCreditNoteLedgerQueryTest {
    private final CreditNoteRepository notes = mock(CreditNoteRepository.class);
    private final RepositoryCreditNoteLedgerQuery query = new RepositoryCreditNoteLedgerQuery(notes);
    @Test
    void should_preserve_all_note_amounts_for_requested_invoice() {
        UUID id = UUID.randomUUID();
        var first = mock(CreditNote.class);
        var second = mock(CreditNote.class);
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
