package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.ApprovePaymentRequest;
import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;

    public ApprovePaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void execute(UUID paymentId, ApprovePaymentRequest request, UUID authenticatedUserId) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        payment.approve(authenticatedUserId, request.commissionPercentage());
        paymentRepository.save(payment);
    }
}
