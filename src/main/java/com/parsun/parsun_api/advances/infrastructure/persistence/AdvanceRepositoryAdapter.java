package com.parsun.parsun_api.advances.infrastructure.persistence;

import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceRepository;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceId;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AdvanceRepositoryAdapter implements AdvanceRepository {

    private final AdvanceJpaRepository jpaRepository;

    public AdvanceRepositoryAdapter(AdvanceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Advance save(Advance advance) {
        var entity = AdvanceJpaMapper.toEntity(advance);
        var saved = jpaRepository.save(entity);
        return AdvanceJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Advance> findById(AdvanceId id) {
        return jpaRepository.findById(id.value())
                .map(AdvanceJpaMapper::toDomain);
    }

    @Override
    public List<Advance> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, String status) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;

        return jpaRepository.findAllWithFilters(from, to, customerId, status)
                .stream()
                .map(AdvanceJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Advance> findActiveByCustomerId(UUID customerId) {
        return jpaRepository.findActiveByCustomerId(customerId)
                .stream()
                .map(AdvanceJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(AdvanceId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public int getNextSequenceForYear(int year) {
        String pattern = "ADV-" + year + "-%";
        return jpaRepository.findMaxSequenceNumberForYear(pattern)
                .map(max -> max + 1)
                .orElse(1);
    }
}
