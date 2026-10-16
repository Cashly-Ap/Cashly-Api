package com.cashly.cashly_api.debit_notes.infrastructure.persistence.adapters;

import com.cashly.cashly_api.debit_notes.infrastructure.persistence.repositories.DebitNoteJpaRepository;
import com.cashly.cashly_api.debit_notes.infrastructure.persistence.repositories.DebitNoteNumberSequenceJpaRepository;

import com.cashly.cashly_api.debit_notes.domain.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.debit_notes.infrastructure.persistence.entities.DebitNoteEntity;
import com.cashly.cashly_api.debit_notes.infrastructure.persistence.entities.DebitNoteNumberSequenceEntity;
import com.cashly.cashly_api.debit_notes.infrastructure.persistence.mappers.DebitNoteJpaMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
                                                UUID customerId) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<DebitNoteEntity> entities = jpaRepository.findAllWithFilters(from, to, customerId);

        return entities.stream()
                .map(DebitNoteJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<DebitNote> findByInvoiceId(UUID invoiceId) {
        List<DebitNoteEntity> entities = jpaRepository.findByInvoiceId(invoiceId);
        return entities.stream()
                .map(DebitNoteJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds) {
        if (invoiceIds.isEmpty()) return Map.of();
        List<Object[]> results = jpaRepository.sumByInvoiceIds(invoiceIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    @Override
    public void delete(DebitNoteId id) {
        jpaRepository.deleteById(id.value());
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
