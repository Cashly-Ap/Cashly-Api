package com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId id);
    List<Payment> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, String status);
    BigDecimal getRefundableAmountByCustomerId(UUID customerId);
    void deleteById(PaymentId id);
    int getNextSequenceForYear(int year);
}
