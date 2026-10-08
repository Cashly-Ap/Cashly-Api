package com.cashly.cashly_api.credit_notes.application.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditNoteFinancialPositionGateway {
    Position findByInvoiceId(UUID invoiceId);

    record Position(BigDecimal paid, BigDecimal creditNotes, BigDecimal debitNotes) {}
}
