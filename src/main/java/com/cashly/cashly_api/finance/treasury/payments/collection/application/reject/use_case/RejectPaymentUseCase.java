package com.cashly.cashly_api.finance.treasury.payments.collection.application.reject.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.reject.request.RejectPaymentRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class RejectPaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public RejectPaymentUseCase(PaymentRepository paymentRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    @Audited(module = "finance", entityType = "PAYMENT", action = "REJECT")
    public PaymentResponse execute(UUID id, RejectPaymentRequest request,
            UUID userId, String userName) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + id));

        payment.reject(request.reason(), userId, userName);

        Payment saved = paymentRepository.save(payment);
        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_RECEIVABLES",
                "REJECT_PAYMENT", "PAYMENT",
                saved.getId().value(), saved.getPaymentNumber().value(), userId));
        return PaymentMapper.toResponse(saved);
    }
}
