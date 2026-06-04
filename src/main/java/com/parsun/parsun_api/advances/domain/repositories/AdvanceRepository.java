package com.parsun.parsun_api.advances.domain.repositories;

import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvanceRepository {

    Advance save(Advance advance);

    Optional<Advance> findById(AdvanceId id);

    List<Advance> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                      UUID customerId, String status);

    List<Advance> findActiveByCustomerId(UUID customerId);

    void deleteById(AdvanceId id);

    int getNextSequenceForYear(int year);
}
