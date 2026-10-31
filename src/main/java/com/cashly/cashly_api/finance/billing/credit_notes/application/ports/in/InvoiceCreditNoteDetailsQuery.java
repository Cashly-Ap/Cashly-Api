package com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InvoiceCreditNoteDetailsQuery {
    List<Note> findByInvoiceId(UUID invoiceId);
    Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds);

    record Note(UUID id, String number, BigDecimal amount, String reason,
            LocalDateTime createdAt) {
    }
}
