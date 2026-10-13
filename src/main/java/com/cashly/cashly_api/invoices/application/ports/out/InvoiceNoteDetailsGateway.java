package com.cashly.cashly_api.invoices.application.ports.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InvoiceNoteDetailsGateway {
    List<Note> findCreditNotes(UUID invoiceId);
    List<Note> findDebitNotes(UUID invoiceId);
    Map<UUID, BigDecimal> sumCreditNotes(List<UUID> invoiceIds);
    Map<UUID, BigDecimal> sumDebitNotes(List<UUID> invoiceIds);

    record Note(UUID id, String number, BigDecimal amount, String reason, LocalDateTime createdAt) {}
}
