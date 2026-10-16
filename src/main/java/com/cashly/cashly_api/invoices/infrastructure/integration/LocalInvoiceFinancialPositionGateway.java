package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.invoices.application.ports.out.InvoiceFinancialPositionGateway;
import com.cashly.cashly_api.payments.collection.application.ports.in.InvoicePaymentTotalQuery;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalInvoiceFinancialPositionGateway implements InvoiceFinancialPositionGateway {
    private final InvoicePaymentTotalQuery payments;
    private final DebitNoteLedgerQuery debitNotes;
    private final CreditNoteLedgerQuery creditNotes;

    public LocalInvoiceFinancialPositionGateway(InvoicePaymentTotalQuery payments,
                                                DebitNoteLedgerQuery debitNotes,
                                                CreditNoteLedgerQuery creditNotes) {
        this.payments = payments;
        this.debitNotes = debitNotes;
        this.creditNotes = creditNotes;
    }

    @Override
    public Position findByInvoiceId(UUID invoiceId) {
        BigDecimal debits = sum(debitNotes.amountsByInvoiceId(invoiceId));
        BigDecimal credits = sum(creditNotes.amountsByInvoiceId(invoiceId));
        return new Position(payments.totalAllocatedByInvoiceId(invoiceId), debits, credits);
    }

    private BigDecimal sum(java.util.List<BigDecimal> amounts) {
        return amounts.stream().map(amount -> amount == null ? BigDecimal.ZERO : amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
