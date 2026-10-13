package com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.adapters;

import com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.repositories.PaymentMethodJpaRepository;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.payment_methods.domain.entities.PaymentMethod;
import com.cashly.cashly_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.entities.PaymentMethodEntity;
import com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.mappers.PaymentMethodJpaMapper;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("name", "code", "createdAt", "updatedAt");

    private final PaymentMethodJpaRepository jpaRepository;

    public PaymentMethodRepositoryAdapter(
            PaymentMethodJpaRepository jpaRepository) {
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
    public Optional<Integer> findMaxCodeNumber() {
        return jpaRepository.findMaxCodeNumber();
    }

    @Override
    public void delete(PaymentMethodId id) {
        jpaRepository.deleteById(id.value());
    }


    @Override
    public PageResponse<PaymentMethod> findAllWithFilters(String search, Boolean active,
                                                           int page, int size,
                                                           String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "name";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<PaymentMethodEntity> result = jpaRepository.findAllWithFilters(search, active, pageable);
        Page<PaymentMethod> mapped = result.map(PaymentMethodJpaMapper::toDomain);
        return PageResponseMapper.from(mapped);
    }
}
