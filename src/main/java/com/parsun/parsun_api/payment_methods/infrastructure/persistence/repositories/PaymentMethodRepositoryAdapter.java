package com.parsun.parsun_api.payment_methods.infrastructure.persistence.repositories;

import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;
import com.parsun.parsun_api.payment_methods.infrastructure.persistence.mappers.PaymentMethodJpaMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private final PaymentMethodJpaRepository jpaRepository;

    public PaymentMethodRepositoryAdapter(PaymentMethodJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
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
    public void delete(PaymentMethodId id) {
        jpaRepository.deleteById(id.value());
    }
}
