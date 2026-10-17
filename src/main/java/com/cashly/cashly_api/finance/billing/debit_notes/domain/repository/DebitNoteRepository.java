package com.cashly.cashly_api.finance.billing.debit_notes.domain.repository;

import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DebitNoteRepository {
    DebitNote save(DebitNote debitNote);
    Optional<DebitNote> findById(DebitNoteId id);
    List<DebitNote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                        UUID customerId);
    List<DebitNote> findByInvoiceId(UUID invoiceId);
    Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds);
    int getNextSequenceForYear(int year);
    void delete(DebitNoteId id);
}
