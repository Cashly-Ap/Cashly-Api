package com.parsun.parsun_api.finances.domain.repositories;

import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId id);
    List<Payment> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                     UUID customerId, String type, String status);
    int getNextSequenceForYear(int year);
}
