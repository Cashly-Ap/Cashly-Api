package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface InvoiceFinancialPositionGateway {
    Position findByInvoiceId(UUID invoiceId);

    record Position(BigDecimal paid, BigDecimal debitNotes, BigDecimal creditNotes) {}
}
