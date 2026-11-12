package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.adapters;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentId;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierPaymentEntity;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.mappers.SupplierPaymentJpaMapper;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.repositories.SpringDataSupplierPaymentRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupplierPaymentRepositoryAdapter
        implements SupplierPaymentRepository {

    private final SpringDataSupplierPaymentRepository jpaRepository;

    public SupplierPaymentRepositoryAdapter(
            SpringDataSupplierPaymentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SupplierPayment save(SupplierPayment payment) {
        SupplierPaymentEntity saved = jpaRepository
                .save(SupplierPaymentJpaMapper.toEntity(payment));
        return SupplierPaymentJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<SupplierPayment> findById(SupplierPaymentId id) {
        return jpaRepository.findById(id.value())
                .map(SupplierPaymentJpaMapper::toDomain);
    }

    @Override
    public List<SupplierPayment> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID supplierId, String status) {
        return jpaRepository
                .findAllWithFilters(dateFrom, dateTo, supplierId, status)
                .stream().map(SupplierPaymentJpaMapper::toDomain).toList();
    }

    @Override
    public void deleteById(SupplierPaymentId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public int getNextSequenceForYear(int year) {
        List<SupplierPaymentEntity> yearPayments = jpaRepository
                .findAllWithFilters(LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31), null, null);
        return yearPayments.size() + 1;
    }
}
