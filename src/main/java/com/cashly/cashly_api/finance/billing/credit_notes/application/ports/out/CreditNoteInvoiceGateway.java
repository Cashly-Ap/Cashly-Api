package com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Map;
import java.util.Set;

public interface CreditNoteInvoiceGateway {
    CreditNoteInvoiceContext find(UUID invoiceId);
    Map<UUID, CreditNoteInvoiceContext> findAll(Set<UUID> invoiceIds);
    void recalculate(UUID invoiceId);

    record CreditNoteInvoiceContext(UUID id, String number, String status,
            String statusLabel, String customerName, String customerRif,
            BigDecimal totalBeforeNotes) {
    }
}
