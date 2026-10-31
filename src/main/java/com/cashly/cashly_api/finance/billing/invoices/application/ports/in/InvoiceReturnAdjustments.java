package com.cashly.cashly_api.finance.billing.invoices.application.ports.in;

import java.math.BigDecimal;

public interface InvoiceReturnAdjustments {
    void apply(InvoiceReturnCommand command);
    void reverse(InvoiceReturnCommand command, BigDecimal totalPaid,
            BigDecimal totalDebitNotes, BigDecimal totalCreditNotes);
}
