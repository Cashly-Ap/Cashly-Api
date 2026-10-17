package com.cashly.cashly_api.finance.billing.debit_notes.application.list.use_case;

import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetDebitNotesUseCaseTest {
    @Test
    void givenDebitNotes_whenListing_thenLoadsInvoiceDetailsInOneBatch() {
        DebitNoteRepository notes = mock(DebitNoteRepository.class);
        DebitNoteInvoiceGateway invoices = mock(DebitNoteInvoiceGateway.class);
        UUID invoiceId = UUID.randomUUID();
        when(notes.findAllWithFilters(null, null, null)).thenReturn(List.of(note(invoiceId)));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of(invoiceId,
                new DebitNoteInvoiceGateway.InvoiceContext(invoiceId, "FAC-1", "PAID", "Pagada", "Acme", "RIF")));

        var result = new GetDebitNotesUseCase(notes, invoices).execute(null, null, null);

        assertEquals("Acme", result.getFirst().customerName());
        assertEquals("RIF", result.getFirst().customerRif());
        assertEquals("FAC-1", result.getFirst().invoiceNumber());
        verify(invoices).findAll(Set.of(invoiceId));
    }

    @Test
    void givenNoMatchingInvoice_whenListing_thenPreservesNullInvoiceDetails() {
        DebitNoteRepository notes = mock(DebitNoteRepository.class);
        DebitNoteInvoiceGateway invoices = mock(DebitNoteInvoiceGateway.class);
        UUID invoiceId = UUID.randomUUID();
        when(notes.findAllWithFilters(null, null, null)).thenReturn(List.of(note(invoiceId)));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of());

        var result = new GetDebitNotesUseCase(notes, invoices).execute(null, null, null);

        assertNull(result.getFirst().customerName());
        assertNull(result.getFirst().customerRif());
        assertNull(result.getFirst().invoiceNumber());
    }

    @Test
    void givenNoDebitNotes_whenListing_thenReturnsEmptyListAndQueriesEmptyBatch() {
        DebitNoteRepository notes = mock(DebitNoteRepository.class);
        DebitNoteInvoiceGateway invoices = mock(DebitNoteInvoiceGateway.class);
        when(notes.findAllWithFilters(null, null, null)).thenReturn(List.of());
        when(invoices.findAll(Set.of())).thenReturn(Map.of());

        var result = new GetDebitNotesUseCase(notes, invoices).execute(null, null, null);

        assertTrue(result.isEmpty());
        verify(invoices).findAll(Set.of());
    }

    private DebitNote note(UUID invoiceId) {
        return DebitNote.reconstitute(DebitNoteId.generate(), DebitNoteNumber.of(2026, 1),
                UUID.randomUUID(), invoiceId, new BigDecimal("15.00"), "Reason", UUID.randomUUID(),
                "User", LocalDateTime.now(), LocalDateTime.now());
    }
}
