package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.finances.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.finances.infrastructure.persistence.mappers.PaymentJpaMapper;
import org.springframework.stereotype.Repository;

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
        var entity = PaymentJpaMapper.toAllocationEntity(allocation);
        var saved = jpaRepository.save(entity);
        return PaymentJpaMapper.toAllocationDomain(saved);
    }

    @Override
    public List<PaymentAllocation> findByPaymentId(UUID paymentId) {
        return jpaRepository.findByPaymentId(paymentId).stream()
                .map(PaymentJpaMapper::toAllocationDomain)
                .toList();
    }

    @Override
    public List<PaymentAllocation> findByInvoiceId(UUID invoiceId) {
        return jpaRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentJpaMapper::toAllocationDomain)
                .toList();
    }

    @Override
    public void deleteByPaymentId(UUID paymentId) {
        jpaRepository.deleteByPaymentId(paymentId);
    }
}
