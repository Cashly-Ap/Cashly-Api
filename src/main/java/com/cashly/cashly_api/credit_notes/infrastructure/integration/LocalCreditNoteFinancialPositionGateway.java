package com.cashly.cashly_api.credit_notes.infrastructure.integration;

import com.cashly.cashly_api.credit_notes.application.contracts.CreditNoteFinancialPositionGateway;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.debit_notes.application.api.DebitNoteLedgerQuery;
import com.cashly.cashly_api.payments.application.api.InvoicePaymentTotalQuery;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalCreditNoteFinancialPositionGateway implements CreditNoteFinancialPositionGateway {
    private final CreditNoteRepository creditNotes;
    private final DebitNoteLedgerQuery debitNotes;
    private final InvoicePaymentTotalQuery payments;

    public LocalCreditNoteFinancialPositionGateway(CreditNoteRepository creditNotes,
                                                    DebitNoteLedgerQuery debitNotes,
                                                    InvoicePaymentTotalQuery payments) {
        this.creditNotes = creditNotes;
        this.debitNotes = debitNotes;
        this.payments = payments;
    }

    @Override
    public Position findByInvoiceId(UUID invoiceId) {
        BigDecimal totalCredits = creditNotes.findByInvoiceId(invoiceId).stream()
                .map(note -> note.getAmount() == null ? BigDecimal.ZERO : note.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDebits = debitNotes.amountsByInvoiceId(invoiceId).stream()
                .map(amount -> amount == null ? BigDecimal.ZERO : amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Position(payments.totalAllocatedByInvoiceId(invoiceId), totalCredits, totalDebits);
    }
}
