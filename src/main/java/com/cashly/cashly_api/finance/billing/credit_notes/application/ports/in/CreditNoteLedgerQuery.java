package com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface CreditNoteLedgerQuery {
    List<BigDecimal> amountsByInvoiceId(UUID invoiceId);
}
