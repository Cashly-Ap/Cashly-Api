package com.parsun.parsun_api.payments.infrastructure.persistence.repositories;

import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.infrastructure.persistence.mappers.PaymentAllocationJpaMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class PaymentAllocationRepositoryAdapter implements PaymentAllocationRepository {

    private final PaymentAllocationJpaRepository jpaRepository;

    public PaymentAllocationRepositoryAdapter(PaymentAllocationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PaymentAllocation save(PaymentAllocation allocation) {
        var entity = PaymentAllocationJpaMapper.toEntity(allocation);
        var saved = jpaRepository.save(entity);
        return PaymentAllocationJpaMapper.toDomain(saved);
    }

    @Override
    public List<PaymentAllocation> findByPaymentId(UUID paymentId) {
        return jpaRepository.findByPaymentId(paymentId).stream()
                .map(PaymentAllocationJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<PaymentAllocation> findByInvoiceId(UUID invoiceId) {
        return jpaRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentAllocationJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByPaymentId(UUID paymentId) {
        jpaRepository.deleteByPaymentId(paymentId);
    }

    @Override
    public BigDecimal getAllocatedAmountByInvoiceId(UUID invoiceId) {
        return jpaRepository.sumAllocatedAmountByInvoiceId(invoiceId);
    }
}
