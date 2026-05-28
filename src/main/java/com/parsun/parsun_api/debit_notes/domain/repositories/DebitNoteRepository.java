package com.parsun.parsun_api.debit_notes.domain.repositories;

import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DebitNoteRepository {
    DebitNote save(DebitNote debitNote);
    Optional<DebitNote> findById(DebitNoteId id);
    List<DebitNote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                        UUID customerId);
    List<DebitNote> findByInvoiceId(UUID invoiceId);
    int getNextSequenceForYear(int year);
    void delete(DebitNoteId id);
}
