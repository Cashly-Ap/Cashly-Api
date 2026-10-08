package com.cashly.cashly_api.invoices.application.api;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Map;
import java.util.Set;

public interface InvoiceNoteApi {
    Context find(UUID invoiceId);
    Map<UUID, Context> findAll(Set<UUID> invoiceIds);
    void recalculate(UUID invoiceId);

    record Context(UUID id, String number, String status, String statusLabel,
                   String customerName, String customerRif, BigDecimal totalBeforeNotes) {}
}
