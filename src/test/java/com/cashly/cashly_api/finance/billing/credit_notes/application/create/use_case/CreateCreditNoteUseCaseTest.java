package com.cashly.cashly_api.finance.billing.credit_notes.application.create.use_case;

import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteEffectsGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteFinancialPositionGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.create.request.CreateCreditNoteRequest;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
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
class CreateCreditNoteUseCaseTest {
    @Mock private CreditNoteRepository notes;
    @Mock private CreditNoteInvoiceGateway invoices;
    @Mock private CreditNoteFinancialPositionGateway position;
    @Mock private CreditNoteEffectsGateway effects;

    private CreateCreditNoteUseCase useCase;
    private UUID invoiceId;
    private UUID customerId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        useCase = new CreateCreditNoteUseCase(notes, invoices, position, effects);
        invoiceId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();
        when(invoices.find(invoiceId)).thenReturn(new CreditNoteInvoiceGateway.CreditNoteInvoiceContext(
                invoiceId, "FAC-001", "PENDING_PAYMENT", "Pendiente", "Customer", "RIF",
                new BigDecimal("100.00")));
    }

    @Test
    void givenValidInvoiceAndPaidSurplus_whenCreatingCreditNote_thenKeepsFinancialEffects() {
        stubNotePersistence();
        when(position.findByInvoiceId(invoiceId)).thenReturn(new CreditNoteFinancialPositionGateway.Position(
                new BigDecimal("150.00"), new BigDecimal("20.00"), new BigDecimal("5.00")));

        useCase.execute(request("30.00"), userId);

        verify(notes).save(any(CreditNote.class));
        verify(invoices).recalculate(invoiceId);
        verify(effects).recordCashOut(eq(new BigDecimal("30.00")), contains("CN-"), any(), eq(userId), eq("User"));
        verify(effects).addCustomerCredit(eq(customerId), eq(new BigDecimal("65.00")), any(),
                contains("CN-"), eq("FAC-001"), eq(userId));
    }

    @Test
    void givenNoSurplus_whenCreatingCreditNote_thenDoesNotAddCustomerCredit() {
        stubNotePersistence();
        when(position.findByInvoiceId(invoiceId)).thenReturn(new CreditNoteFinancialPositionGateway.Position(
                new BigDecimal("10.00"), new BigDecimal("20.00"), BigDecimal.ZERO));

        useCase.execute(request("30.00"), userId);

        verify(effects, never()).addCustomerCredit(any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenCancelledInvoice_whenCreatingCreditNote_thenRejectsBeforeAnyEffect() {
        when(invoices.find(invoiceId)).thenReturn(new CreditNoteInvoiceGateway.CreditNoteInvoiceContext(
                invoiceId, "FAC-001", "CANCELLED", "Anulada", "Customer", "RIF",
                new BigDecimal("100.00")));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request("30.00"), userId));

        verify(notes, never()).save(any());
        verify(effects, never()).recordCashOut(any(), any(), any(), any(), any());
        verify(invoices, never()).recalculate(any());
    }

    @Test
    void givenFullyReturnedInvoice_whenCreatingCreditNote_thenRejectsBeforeAnyEffect() {
        when(invoices.find(invoiceId)).thenReturn(new CreditNoteInvoiceGateway.CreditNoteInvoiceContext(
                invoiceId, "FAC-001", "FULLY_RETURNED", "Devuelta", "Customer", "RIF",
                new BigDecimal("100.00")));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request("30.00"), userId));

        verify(notes, never()).save(any());
        verifyNoInteractions(effects);
    }

    @Test
    void givenCreditNotesExceedInvoiceTotal_whenCreatingCreditNote_thenClampsEffectiveTotalToZero() {
        stubNotePersistence();
        when(position.findByInvoiceId(invoiceId)).thenReturn(new CreditNoteFinancialPositionGateway.Position(
                new BigDecimal("10.00"), new BigDecimal("150.00"), BigDecimal.ZERO));

        useCase.execute(request("30.00"), userId);

        verify(effects).addCustomerCredit(eq(customerId), eq(new BigDecimal("10.00")), any(),
                contains("CN-"), eq("FAC-001"), eq(userId));
    }

    @Test
    void givenMissingInvoice_whenCreatingCreditNote_thenRejectsWithoutEffects() {
        when(invoices.find(invoiceId)).thenThrow(new IllegalArgumentException("Invoice not found: " + invoiceId));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request("30.00"), userId));

        verify(notes, never()).save(any());
        verify(effects, never()).recordCashOut(any(), any(), any(), any(), any());
    }

    private CreateCreditNoteRequest request(String amount) {
        return new CreateCreditNoteRequest(customerId, invoiceId, new BigDecimal(amount), "Damaged product");
    }

    private void stubNotePersistence() {
        when(notes.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(notes.save(any(CreditNote.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
