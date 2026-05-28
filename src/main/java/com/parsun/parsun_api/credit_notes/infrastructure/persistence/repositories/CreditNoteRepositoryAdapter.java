package com.parsun.parsun_api.credit_notes.infrastructure.persistence.repositories;

import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import com.parsun.parsun_api.credit_notes.infrastructure.persistence.entities.CreditNoteNumberSequenceEntity;
import com.parsun.parsun_api.credit_notes.infrastructure.persistence.mappers.CreditNoteJpaMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CreditNoteRepositoryAdapter implements CreditNoteRepository {

    private final CreditNoteJpaRepository jpaRepository;
    private final CreditNoteNumberSequenceJpaRepository sequenceJpaRepository;

    public CreditNoteRepositoryAdapter(CreditNoteJpaRepository jpaRepository,
                                       CreditNoteNumberSequenceJpaRepository sequenceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public CreditNote save(CreditNote creditNote) {
        var entity = CreditNoteJpaMapper.toEntity(creditNote);
        var saved = jpaRepository.save(entity);
        return CreditNoteJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<CreditNote> findById(CreditNoteId id) {
        return jpaRepository.findById(id.value())
                .map(CreditNoteJpaMapper::toDomain);
    }

    @Override
    public List<CreditNote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                                UUID customerId) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<CreditNoteEntity> entities = jpaRepository.findAllWithFilters(from, to, customerId);

        return entities.stream()
                .map(CreditNoteJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<CreditNote> findByInvoiceId(UUID invoiceId) {
        List<CreditNoteEntity> entities = jpaRepository.findByInvoiceId(invoiceId);
        return entities.stream()
                .map(CreditNoteJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(CreditNoteId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public int getNextSequenceForYear(int year) {
        CreditNoteNumberSequenceEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    CreditNoteNumberSequenceEntity newSeq = new CreditNoteNumberSequenceEntity();
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
