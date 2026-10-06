package com.cashly.cashly_api.payments.application.usecases;

import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.payments.application.dto.request.RejectPaymentRequest;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.application.mappers.PaymentMapper;
import com.cashly.cashly_api.payments.domain.entities.Payment;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class RejectPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public RejectPaymentUseCase(PaymentRepository paymentRepository,
                                  NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResponse execute(UUID id, RejectPaymentRequest request, UUID userId, String userName) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        payment.reject(request.reason(), userId, userName);

        Payment saved = paymentRepository.save(payment);
        notificationService.notifyCustomerPaymentRejected(payment.getCustomerId(), id, request.reason());
        return PaymentMapper.toResponse(saved);
    }
}
