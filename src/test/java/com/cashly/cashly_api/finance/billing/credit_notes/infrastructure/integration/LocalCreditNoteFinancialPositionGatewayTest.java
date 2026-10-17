package com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.InvoicePaymentTotalQuery;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocalCreditNoteFinancialPositionGatewayTest {
    @Test
    void givenPaymentAndNotes_whenReadingPosition_thenSumsCreditAndDebitAmounts() {
        CreditNoteRepository credits = mock(CreditNoteRepository.class);
        DebitNoteLedgerQuery debits = mock(DebitNoteLedgerQuery.class);
        InvoicePaymentTotalQuery payments = mock(InvoicePaymentTotalQuery.class);
        UUID invoiceId = UUID.randomUUID();
        CreditNote first = mock(CreditNote.class);
        CreditNote second = mock(CreditNote.class);
        when(first.getAmount()).thenReturn(new BigDecimal("12.50"));
        when(second.getAmount()).thenReturn(new BigDecimal("7.50"));
        when(credits.findByInvoiceId(invoiceId)).thenReturn(List.of(first, second));
        when(debits.amountsByInvoiceId(invoiceId)).thenReturn(Arrays.asList(new BigDecimal("4.00"), null));
        when(payments.totalAllocatedByInvoiceId(invoiceId)).thenReturn(new BigDecimal("90.00"));

        var position = new LocalCreditNoteFinancialPositionGateway(credits, debits, payments)
                .findByInvoiceId(invoiceId);

        assertEquals(new BigDecimal("90.00"), position.paid());
        assertEquals(new BigDecimal("20.00"), position.creditNotes());
        assertEquals(new BigDecimal("4.00"), position.debitNotes());
    }
}
