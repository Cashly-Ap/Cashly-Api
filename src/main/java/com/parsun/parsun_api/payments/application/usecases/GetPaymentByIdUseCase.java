package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;

import java.util.UUID;

public class GetPaymentByIdUseCase {

    private final PaymentRepository paymentRepository;

    public GetPaymentByIdUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse execute(UUID id) {
        return paymentRepository.findById(PaymentId.of(id))
                .map(PaymentMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
    }
}
