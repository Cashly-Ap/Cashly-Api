package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.application.contracts.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetDebitNoteByIdUseCaseTest {
    @Test
    void givenDebitNote_whenFinding_thenEnrichesFromInvoiceContract() {
        DebitNoteRepository notes = mock(DebitNoteRepository.class);
        DebitNoteInvoiceGateway invoices = mock(DebitNoteInvoiceGateway.class);
        UUID id = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        when(notes.findById(DebitNoteId.of(id))).thenReturn(Optional.of(note(id, invoiceId)));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of(invoiceId,
                new DebitNoteInvoiceGateway.InvoiceContext(invoiceId, "FAC-1", "PAID", "Pagada", "Acme", "RIF")));

        var response = new GetDebitNoteByIdUseCase(notes, invoices).execute(id);

        assertEquals(id, response.id());
        assertEquals("Acme", response.customerName());
        assertEquals("RIF", response.customerRif());
        assertEquals("FAC-1", response.invoiceNumber());
    }

    @Test
    void givenMissingInvoice_whenFinding_thenReturnsNullInvoiceDetails() {
        DebitNoteRepository notes = mock(DebitNoteRepository.class);
        DebitNoteInvoiceGateway invoices = mock(DebitNoteInvoiceGateway.class);
        UUID id = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        when(notes.findById(DebitNoteId.of(id))).thenReturn(Optional.of(note(id, invoiceId)));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of());

        var response = new GetDebitNoteByIdUseCase(notes, invoices).execute(id);

        assertNull(response.customerName());
        assertNull(response.customerRif());
        assertNull(response.invoiceNumber());
    }

    @Test
    void givenMissingDebitNote_whenFinding_thenRaisesNotFound() {
        DebitNoteRepository notes = mock(DebitNoteRepository.class);
        DebitNoteInvoiceGateway invoices = mock(DebitNoteInvoiceGateway.class);
        UUID id = UUID.randomUUID();
        when(notes.findById(DebitNoteId.of(id))).thenReturn(Optional.empty());

        var exception = assertThrows(IllegalArgumentException.class,
                () -> new GetDebitNoteByIdUseCase(notes, invoices).execute(id));

        assertEquals("Debit note not found: " + id, exception.getMessage());
        verifyNoInteractions(invoices);
    }

    private DebitNote note(UUID id, UUID invoiceId) {
        return DebitNote.reconstitute(DebitNoteId.of(id), DebitNoteNumber.of(2026, 1), UUID.randomUUID(),
                invoiceId, new BigDecimal("15.00"), "Reason", UUID.randomUUID(), "User",
                LocalDateTime.now(), LocalDateTime.now());
    }
}
