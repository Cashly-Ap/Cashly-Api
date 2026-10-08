package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.application.contracts.DebitNoteCashGateway;
import com.cashly.cashly_api.debit_notes.application.contracts.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDebitNoteUseCaseTest {
    @Mock private DebitNoteRepository notes;
    @Mock private DebitNoteInvoiceGateway invoices;
    @Mock private DebitNoteCashGateway cash;

    private CreateDebitNoteUseCase useCase;
    private UUID invoiceId;
    private UUID customerId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        useCase = new CreateDebitNoteUseCase(notes, invoices, cash);
        invoiceId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();
        when(invoices.find(invoiceId)).thenReturn(new DebitNoteInvoiceGateway.InvoiceContext(
                invoiceId, "FAC-1", "PENDING_PAYMENT", "Pendiente", "Customer", "RIF"));
    }

    @Test
    void givenValidInvoice_whenCreatingDebitNote_thenRecordsCashInAndRecalculates() {
        when(notes.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(notes.save(any(DebitNote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(request(), userId);

        verify(notes).save(any(DebitNote.class));
        verify(cash).recordCashIn(eq(new BigDecimal("50.00")), contains("DN-"), any(), eq(userId), eq("User"));
        verify(invoices).recalculate(invoiceId);
    }

    @Test
    void givenCancelledInvoice_whenCreatingDebitNote_thenRejectsBeforeSaving() {
        when(invoices.find(invoiceId)).thenReturn(new DebitNoteInvoiceGateway.InvoiceContext(
                invoiceId, "FAC-1", "CANCELLED", "Anulada", "Customer", "RIF"));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request(), userId));

        verify(notes, never()).save(any());
        verifyNoInteractions(cash);
        verify(invoices, never()).recalculate(any());
    }

    @Test
    void givenFullyReturnedInvoice_whenCreatingDebitNote_thenRejectsBeforeSaving() {
        when(invoices.find(invoiceId)).thenReturn(new DebitNoteInvoiceGateway.InvoiceContext(
                invoiceId, "FAC-1", "FULLY_RETURNED", "Devuelta", "Customer", "RIF"));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request(), userId));

        verify(notes, never()).save(any());
        verifyNoInteractions(cash);
    }

    private CreateDebitNoteRequest request() {
        return new CreateDebitNoteRequest(customerId, invoiceId, new BigDecimal("50.00"), "Price adjustment");
    }
}
