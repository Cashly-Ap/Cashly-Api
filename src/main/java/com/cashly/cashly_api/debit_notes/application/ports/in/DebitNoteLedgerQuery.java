package com.cashly.cashly_api.debit_notes.application.ports.in;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface DebitNoteLedgerQuery {
    List<BigDecimal> amountsByInvoiceId(UUID invoiceId);
}
