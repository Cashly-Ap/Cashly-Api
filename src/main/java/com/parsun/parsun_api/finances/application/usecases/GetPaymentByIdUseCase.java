package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PaymentDetailResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.finances.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.finances.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentMethodEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.PaymentMethodJpaRepository;

import java.util.List;
import java.util.UUID;

public class GetPaymentByIdUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    public GetPaymentByIdUseCase(PaymentRepository paymentRepository,
                                 PaymentAllocationRepository allocationRepository,
                                 PaymentMethodJpaRepository paymentMethodJpaRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.paymentMethodJpaRepository = paymentMethodJpaRepository;
    }

    public PaymentDetailResponse execute(UUID paymentId) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        List<PaymentAllocation> allocations = allocationRepository.findByPaymentId(paymentId);
        String paymentMethodName = paymentMethodJpaRepository.findById(payment.getPaymentMethodId())
                .map(PaymentMethodEntity::getName)
                .orElse(null);

        return FinanceMapper.toPaymentDetailResponse(payment, allocations, paymentMethodName, null);
    }
}
