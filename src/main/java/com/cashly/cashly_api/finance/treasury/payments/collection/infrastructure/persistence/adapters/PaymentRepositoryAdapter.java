package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.adapters;

import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.repositories.PaymentJpaRepository;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.entities.PaymentEntity;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.mappers.PaymentJpaMapper;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository,
            EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Payment insert(Payment payment) {
        PaymentEntity entity = PaymentJpaMapper.toEntity(payment);
        entityManager.persist(entity);
        return PaymentJpaMapper.toDomain(entity);
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
    public List<Payment> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status) {
        return jpaRepository
                .findAllWithFilters(dateFrom, dateTo, customerId, status)
                .stream().map(PaymentJpaMapper::toDomain).toList();
    }

    @Override
    public PageResponse<Payment> findPageWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status, String search,
            int page, int size, String orderBy, String orderDir) {
        Page<PaymentEntity> result = jpaRepository.findPageWithFilters(dateFrom,
                dateTo, customerId, status, searchPattern(search),
                PageRequest.of(page, size, sort(orderBy, orderDir)));
        List<Payment> content = result.getContent().stream()
                .map(PaymentJpaMapper::toDomain).toList();
        return new PageResponse<>(content, result.getTotalElements(),
                result.getTotalPages(), result.getSize(), result.getNumber(),
                result.isFirst(), result.isLast(), content.isEmpty());
    }

    private static String searchPattern(String search) {
        if (search == null || search.isBlank()) {
            return "";
        }
        return "%" + search.trim().toLowerCase() + "%";
    }

    private static Sort sort(String orderBy, String orderDir) {
        String property = switch (orderBy == null ? "" : orderBy) {
            case "paymentNumber" -> "paymentNumber";
            case "amount" -> "amount";
            case "status" -> "status";
            case "paymentDate" -> "paymentDate";
            default -> "createdAt";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(orderDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property).and(Sort.by(direction, "id"));
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
        return (int) jpaRepository.countByPaymentDateBetween(
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)) + 1;
    }
}
