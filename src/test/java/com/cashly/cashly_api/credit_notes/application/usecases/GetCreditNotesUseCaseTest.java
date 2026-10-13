package com.cashly.cashly_api.credit_notes.application.usecases;

import com.cashly.cashly_api.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.credit_notes.domain.entities.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetCreditNotesUseCaseTest {
    @Test
    void givenNotes_whenListing_thenLoadsInvoiceDetailsInOneBatch() {
        CreditNoteRepository notes = mock(CreditNoteRepository.class);
        CreditNoteInvoiceGateway invoices = mock(CreditNoteInvoiceGateway.class);
        UUID invoiceId = UUID.randomUUID();
        CreditNote note = note(invoiceId);
        when(notes.findAllWithFilters(null, null, null)).thenReturn(List.of(note));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of(invoiceId,
                new CreditNoteInvoiceGateway.CreditNoteInvoiceContext(invoiceId, "FAC-1", "PENDING_PAYMENT",
                        "Pendiente", "Acme", "RIF-1", new BigDecimal("100.00"))));

        var result = new GetCreditNotesUseCase(notes, invoices).execute(null, null, null);

        assertEquals("Acme", result.getFirst().customerName());
        assertEquals("RIF-1", result.getFirst().customerRif());
        assertEquals("FAC-1", result.getFirst().invoiceNumber());
        verify(invoices).findAll(Set.of(invoiceId));
    }

    @Test
    void givenNoInvoicesInBatch_whenListing_thenPreservesNullInvoiceDetails() {
        CreditNoteRepository notes = mock(CreditNoteRepository.class);
        CreditNoteInvoiceGateway invoices = mock(CreditNoteInvoiceGateway.class);
        UUID invoiceId = UUID.randomUUID();
        when(notes.findAllWithFilters(null, null, null)).thenReturn(List.of(note(invoiceId)));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of());

        var result = new GetCreditNotesUseCase(notes, invoices).execute(null, null, null);

        assertNull(result.getFirst().customerName());
        assertNull(result.getFirst().customerRif());
        assertNull(result.getFirst().invoiceNumber());
    }

    private CreditNote note(UUID invoiceId) {
        return CreditNote.reconstitute(CreditNoteId.generate(), CreditNoteNumber.of(2026, 1),
                UUID.randomUUID(), invoiceId, new BigDecimal("15.00"), "Reason", UUID.randomUUID(),
                "User", LocalDateTime.now(), LocalDateTime.now());
    }
}
