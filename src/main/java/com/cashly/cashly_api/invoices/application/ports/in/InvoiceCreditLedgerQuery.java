package com.cashly.cashly_api.invoices.application.ports.in;
import java.util.List;
import java.util.UUID;
public interface InvoiceCreditLedgerQuery {
    List<InvoiceCreditLedgerEntry> findByCustomerId(UUID customerId);
    List<InvoiceCreditLedgerEntry> findAll();
}
