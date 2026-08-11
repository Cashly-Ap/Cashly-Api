package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.ApprovePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public ApprovePaymentUseCase(PaymentRepository paymentRepository,
                                   NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResponse execute(UUID id, ApprovePaymentRequest request, UUID userId, String userName) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        payment.approve(request.reason(), userId, userName);

        Payment saved = paymentRepository.save(payment);
        notificationService.notifyCustomerPaymentApproved(payment.getCustomerId(), id);
        return PaymentMapper.toResponse(saved);
    }
}
