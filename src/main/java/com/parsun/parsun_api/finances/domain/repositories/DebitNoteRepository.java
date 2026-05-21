package com.parsun.parsun_api.finances.domain.repositories;

import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DebitNoteRepository {
    DebitNote save(DebitNote debitNote);
    Optional<DebitNote> findById(DebitNoteId id);
    List<DebitNote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                       UUID customerId, String status);
    int getNextSequenceForYear(int year);
}
