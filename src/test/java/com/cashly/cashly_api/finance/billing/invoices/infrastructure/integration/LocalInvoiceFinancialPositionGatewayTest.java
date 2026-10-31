package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.InvoicePaymentTotalQuery;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocalInvoiceFinancialPositionGatewayTest {
    @Test
    void givenCreditDebitAndPaymentLedgers_whenRequested_thenReturnsTheirTotals() {
        InvoicePaymentTotalQuery payments = mock(
                InvoicePaymentTotalQuery.class);
        DebitNoteLedgerQuery debits = mock(DebitNoteLedgerQuery.class);
        CreditNoteLedgerQuery credits = mock(CreditNoteLedgerQuery.class);
        UUID invoiceId = UUID.randomUUID();
        when(payments.totalAllocatedByInvoiceId(invoiceId))
                .thenReturn(new BigDecimal("50.00"));
        when(debits.amountsByInvoiceId(invoiceId))
                .thenReturn(Arrays.asList(new BigDecimal("10.00"), null));
        when(credits.amountsByInvoiceId(invoiceId))
                .thenReturn(Arrays.asList(new BigDecimal("4.00"), null));

        var position = new LocalInvoiceFinancialPositionGateway(payments,
                debits, credits).findByInvoiceId(invoiceId);

        assertEquals(new BigDecimal("50.00"), position.paid());
        assertEquals(new BigDecimal("10.00"), position.debitNotes());
        assertEquals(new BigDecimal("4.00"), position.creditNotes());
    }
}
