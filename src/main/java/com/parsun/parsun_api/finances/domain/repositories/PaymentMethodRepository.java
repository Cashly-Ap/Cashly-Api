package com.parsun.parsun_api.finances.domain.repositories;

import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository {
    PaymentMethod save(PaymentMethod paymentMethod);
    Optional<PaymentMethod> findById(PaymentMethodId id);
    List<PaymentMethod> findAll(Boolean active);
    Optional<PaymentMethod> findByCode(String code);
    boolean hasPayments(PaymentMethodId id);
    void delete(PaymentMethodId id);
}
