package com.cashly.cashly_api.sales.invoices.credit_notes.application.ports.in;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public interface CreditNoteLedgerQuery {
    List<BigDecimal> amountsByInvoiceId(UUID invoiceId);
    Map<UUID, BigDecimal> totalsByInvoiceIds(List<UUID> invoiceIds);
}
