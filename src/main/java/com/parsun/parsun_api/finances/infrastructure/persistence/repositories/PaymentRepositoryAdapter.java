package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentNumberSequenceEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.mappers.PaymentJpaMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentNumberSequenceJpaRepository sequenceJpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository,
                                    PaymentNumberSequenceJpaRepository sequenceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        var entity = PaymentJpaMapper.toEntity(payment);
        var saved = jpaRepository.save(entity);
        return PaymentJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpaRepository.findById(id.value())
                .map(PaymentJpaMapper::toDomain);
    }

    @Override
    public List<Payment> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                            UUID customerId, String type, String status) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<PaymentEntity> entities = jpaRepository.findAllWithFilters(from, to, customerId, type, status);

        return entities.stream()
                .map(PaymentJpaMapper::toDomain)
                .toList();
    }

    @Override
    public int getNextSequenceForYear(int year) {
        PaymentNumberSequenceEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    PaymentNumberSequenceEntity newSeq = new PaymentNumberSequenceEntity();
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
