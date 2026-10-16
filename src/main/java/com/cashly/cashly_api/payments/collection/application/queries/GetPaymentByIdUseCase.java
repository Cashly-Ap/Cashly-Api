package com.cashly.cashly_api.payments.collection.application.queries;

import com.cashly.cashly_api.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.payments.collection.domain.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;

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
