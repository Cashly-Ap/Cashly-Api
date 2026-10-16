package com.cashly.cashly_api.credit_notes.application.get.use_case;

import com.cashly.cashly_api.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetCreditNoteByIdUseCaseTest {
    @Test
    void givenCreditNote_whenFinding_thenEnrichesFromNeutralInvoiceContext() {
        CreditNoteRepository notes = mock(CreditNoteRepository.class);
        CreditNoteInvoiceGateway invoices = mock(CreditNoteInvoiceGateway.class);
        UUID noteId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        CreditNote note = CreditNote.reconstitute(CreditNoteId.of(noteId), CreditNoteNumber.of(2026, 1),
                UUID.randomUUID(), invoiceId, new BigDecimal("15.00"), "Reason", UUID.randomUUID(),
                "User", LocalDateTime.now(), LocalDateTime.now());
        when(notes.findById(CreditNoteId.of(noteId))).thenReturn(Optional.of(note));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of(invoiceId,
                new CreditNoteInvoiceGateway.CreditNoteInvoiceContext(invoiceId, "FAC-1", "PAID",
                        "Pagada", "Acme", "RIF-1", new BigDecimal("15.00"))));

        var response = new GetCreditNoteByIdUseCase(notes, invoices).execute(noteId);

        assertEquals("Acme", response.customerName());
        assertEquals("RIF-1", response.customerRif());
        assertEquals("FAC-1", response.invoiceNumber());
    }

    @Test
    void givenInvoiceAbsent_whenFinding_thenReturnsNullInvoiceDetails() {
        CreditNoteRepository notes = mock(CreditNoteRepository.class);
        CreditNoteInvoiceGateway invoices = mock(CreditNoteInvoiceGateway.class);
        UUID noteId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        CreditNote note = CreditNote.reconstitute(CreditNoteId.of(noteId), CreditNoteNumber.of(2026, 1),
                UUID.randomUUID(), invoiceId, new BigDecimal("15.00"), "Reason", UUID.randomUUID(),
                "User", LocalDateTime.now(), LocalDateTime.now());
        when(notes.findById(CreditNoteId.of(noteId))).thenReturn(Optional.of(note));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of());

        var response = new GetCreditNoteByIdUseCase(notes, invoices).execute(noteId);

        assertNull(response.customerName());
        assertNull(response.customerRif());
        assertNull(response.invoiceNumber());
    }
}
