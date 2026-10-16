package com.cashly.cashly_api.payment_methods.domain.repository;

import com.cashly.cashly_api.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository {
    PaymentMethod save(PaymentMethod paymentMethod);
    Optional<PaymentMethod> findById(PaymentMethodId id);
    List<PaymentMethod> findAll(Boolean active);
    Optional<PaymentMethod> findByCode(String code);
    Optional<Integer> findMaxCodeNumber();
    void delete(PaymentMethodId id);
    PageResponse<PaymentMethod> findAllWithFilters(String search, Boolean active,
                                                    int page, int size,
                                                    String orderBy, String orderDir);
}
