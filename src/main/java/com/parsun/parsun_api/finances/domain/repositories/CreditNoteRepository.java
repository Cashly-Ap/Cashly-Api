package com.parsun.parsun_api.finances.domain.repositories;

import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditNoteRepository {
    CreditNote save(CreditNote creditNote);
    Optional<CreditNote> findById(CreditNoteId id);
    List<CreditNote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                        UUID customerId, String status);
    int getNextSequenceForYear(int year);
    void delete(CreditNoteId id);
}
