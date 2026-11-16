package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.adapters;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.mappers.SupplierPaymentJpaMapper;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.repositories.SpringDataSupplierPaymentAllocationRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class SupplierPaymentAllocationRepositoryAdapter
        implements SupplierPaymentAllocationRepository {

    private final SpringDataSupplierPaymentAllocationRepository jpaRepository;

    public SupplierPaymentAllocationRepositoryAdapter(
            SpringDataSupplierPaymentAllocationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SupplierPaymentAllocation save(
            SupplierPaymentAllocation allocation) {
        return SupplierPaymentJpaMapper.toDomain(jpaRepository
                .save(SupplierPaymentJpaMapper.toEntity(allocation)));
    }

    @Override
    public List<SupplierPaymentAllocation> findByPaymentId(UUID paymentId) {
        return jpaRepository.findByPaymentId(paymentId).stream()
                .map(SupplierPaymentJpaMapper::toDomain).toList();
    }

    @Override
    public List<SupplierPaymentAllocation> findByPurchaseId(
            UUID purchaseId) {
        return jpaRepository.findByPurchaseId(purchaseId).stream()
                .map(SupplierPaymentJpaMapper::toDomain).toList();
    }

    @Override
    public BigDecimal getAllocatedAmountByPurchaseId(UUID purchaseId) {
        return jpaRepository.sumAllocatedAmountByPurchaseId(purchaseId);
    }

    @Override
    public void deleteByPaymentId(UUID paymentId) {
        jpaRepository.deleteByPaymentId(paymentId);
    }
}
