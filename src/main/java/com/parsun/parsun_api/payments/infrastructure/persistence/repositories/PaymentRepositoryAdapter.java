package com.parsun.parsun_api.payments.infrastructure.persistence.repositories;

import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.infrastructure.persistence.entities.PaymentEntity;
import com.parsun.parsun_api.payments.infrastructure.persistence.mappers.PaymentJpaMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentJpaMapper.toEntity(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return PaymentJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpaRepository.findById(id.value())
                .map(PaymentJpaMapper::toDomain);
    }

    @Override
    public List<Payment> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, String status) {
        return jpaRepository.findAllWithFilters(dateFrom, dateTo, customerId, status)
                .stream()
                .map(PaymentJpaMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal getRefundableAmountByCustomerId(UUID customerId) {
        return jpaRepository.getRefundableAmountByCustomerId(customerId);
    }

    @Override
    public void deleteById(PaymentId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public int getNextSequenceForYear(int year) {
        // Simplified sequence: count existing payments for the year + 1
        // This can be replaced with a dedicated sequence table later
        List<PaymentEntity> yearPayments = jpaRepository.findAllWithFilters(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31),
                null, null);
        return yearPayments.size() + 1;
    }
}
