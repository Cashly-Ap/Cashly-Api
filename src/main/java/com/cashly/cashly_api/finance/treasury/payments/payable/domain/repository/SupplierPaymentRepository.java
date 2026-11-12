package com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierPaymentRepository {
    SupplierPayment save(SupplierPayment payment);
    Optional<SupplierPayment> findById(SupplierPaymentId id);
    List<SupplierPayment> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID supplierId, String status);
    void deleteById(SupplierPaymentId id);
    int getNextSequenceForYear(int year);
}
