package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.payments.application.dto.request.UpdatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class UpdatePaymentUseCase {

    private final PaymentRepository paymentRepository;

    public UpdatePaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse execute(UUID id, UpdatePaymentRequest request) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        // Apply partial updates
        if (request.referenceNumber() != null) {
            // referenceNumber is not mutable via domain entity; this is a placeholder
            // for future domain behavior if needed
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }
}
