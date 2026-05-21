package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.entities.ReturnLine;
import com.parsun.parsun_api.finances.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnId;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnType;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnLineEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnNumberSequenceEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.mappers.ReturnJpaMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReturnRepositoryAdapter implements ReturnRepository {

    private final ReturnJpaRepository returnJpaRepository;
    private final ReturnLineJpaRepository returnLineJpaRepository;
    private final ReturnNumberSequenceJpaRepository sequenceJpaRepository;

    public ReturnRepositoryAdapter(ReturnJpaRepository returnJpaRepository,
                                   ReturnLineJpaRepository returnLineJpaRepository,
                                   ReturnNumberSequenceJpaRepository sequenceJpaRepository) {
        this.returnJpaRepository = returnJpaRepository;
        this.returnLineJpaRepository = returnLineJpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public Return save(Return returnEntity) {
        var entity = ReturnJpaMapper.toEntity(returnEntity);
        var saved = returnJpaRepository.save(entity);
        return ReturnJpaMapper.toDomain(saved);
    }

    @Override
    public ReturnLine saveLine(ReturnLine line) {
        var entity = ReturnJpaMapper.toLineEntity(line);
        var saved = returnLineJpaRepository.save(entity);
        return ReturnJpaMapper.toLineDomain(saved);
    }

    @Override
    public Optional<Return> findById(ReturnId id) {
        return returnJpaRepository.findById(id.value())
                .map(ReturnJpaMapper::toDomain);
    }

    @Override
    public List<Return> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                            UUID customerId, ReturnType returnType) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        String type = returnType != null ? returnType.name() : null;

        List<ReturnEntity> entities = returnJpaRepository.findAllWithFilters(from, to, customerId, type);

        return entities.stream()
                .map(ReturnJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Return> findByInvoiceId(UUID invoiceId) {
        return returnJpaRepository.findByInvoiceId(invoiceId)
                .map(ReturnJpaMapper::toDomain);
    }

    @Override
    public int getNextSequenceForYear(int year) {
        ReturnNumberSequenceEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    ReturnNumberSequenceEntity newSeq = new ReturnNumberSequenceEntity();
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
