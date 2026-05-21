package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.DebitNoteEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.DebitNoteNumberSequenceEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.mappers.DebitNoteJpaMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DebitNoteRepositoryAdapter implements DebitNoteRepository {

    private final DebitNoteJpaRepository jpaRepository;
    private final DebitNoteNumberSequenceJpaRepository sequenceJpaRepository;

    public DebitNoteRepositoryAdapter(DebitNoteJpaRepository jpaRepository,
                                      DebitNoteNumberSequenceJpaRepository sequenceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public DebitNote save(DebitNote debitNote) {
        var entity = DebitNoteJpaMapper.toEntity(debitNote);
        var saved = jpaRepository.save(entity);
        return DebitNoteJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<DebitNote> findById(DebitNoteId id) {
        return jpaRepository.findById(id.value())
                .map(DebitNoteJpaMapper::toDomain);
    }

    @Override
    public List<DebitNote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                               UUID customerId, String status) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<DebitNoteEntity> entities = jpaRepository.findAllWithFilters(from, to, customerId, status);

        return entities.stream()
                .map(DebitNoteJpaMapper::toDomain)
                .toList();
    }

    @Override
    public int getNextSequenceForYear(int year) {
        DebitNoteNumberSequenceEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    DebitNoteNumberSequenceEntity newSeq = new DebitNoteNumberSequenceEntity();
                    newSeq.setYear(year);
                    newSeq.setNextValue(0);
                    return newSeq;
                });

        int next = sequence.getNextValue() + 1;
        sequence.setNextValue(next);
        sequenceJpaRepository.save(sequence);
        return next;
    }
}
