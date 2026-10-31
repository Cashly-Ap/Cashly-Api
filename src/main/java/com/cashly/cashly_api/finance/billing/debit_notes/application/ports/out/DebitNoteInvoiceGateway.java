package com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DebitNoteInvoiceGateway {
    InvoiceContext find(UUID invoiceId);
    Map<UUID, InvoiceContext> findAll(Set<UUID> invoiceIds);
    void recalculate(UUID invoiceId);

    record InvoiceContext(UUID id, String number, String status,
            String statusLabel, String customerName, String customerRif) {
    }
}
