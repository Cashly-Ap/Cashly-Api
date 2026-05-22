package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId;
import com.parsun.parsun_api.finances.infrastructure.persistence.mappers.PaymentMethodJpaMapper;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.PaymentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private final PaymentMethodJpaRepository jpaRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentMethodRepositoryAdapter(PaymentMethodJpaRepository jpaRepository, PaymentJpaRepository paymentJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) {
        var entity = PaymentMethodJpaMapper.toEntity(paymentMethod);
        var saved = jpaRepository.save(entity);
        return PaymentMethodJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<PaymentMethod> findById(PaymentMethodId id) {
        return jpaRepository.findById(id.value())
                .map(PaymentMethodJpaMapper::toDomain);
    }

    @Override
    public List<PaymentMethod> findAll(Boolean active) {
        return jpaRepository.findAllActive(active).stream()
                .map(PaymentMethodJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PaymentMethod> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(PaymentMethodJpaMapper::toDomain);
    }

    @Override
    public boolean hasPayments(PaymentMethodId id) {
        return paymentJpaRepository.existsByPaymentMethodId(id.value());
    }

    @Override
    public void delete(PaymentMethodId id) {
        jpaRepository.deleteById(id.value());
    }
}
