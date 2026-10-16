package com.cashly.cashly_api.credit_notes.domain.repository;

import com.cashly.cashly_api.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CreditNoteRepository {
    CreditNote save(CreditNote creditNote);
    Optional<CreditNote> findById(CreditNoteId id);
    List<CreditNote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                        UUID customerId);
    List<CreditNote> findByInvoiceId(UUID invoiceId);
    Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds);
    int getNextSequenceForYear(int year);
    void delete(CreditNoteId id);
}
