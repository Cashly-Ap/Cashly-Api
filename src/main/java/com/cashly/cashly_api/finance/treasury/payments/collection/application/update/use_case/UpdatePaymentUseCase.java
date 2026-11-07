package com.cashly.cashly_api.finance.treasury.payments.collection.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.update.request.UpdatePaymentRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class UpdatePaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdatePaymentUseCase(PaymentRepository paymentRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse execute(UUID id, UpdatePaymentRequest request) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + id));

        String referenceNumber = request.referenceNumber() != null
                ? request.referenceNumber()
                : payment.getReferenceNumber();
        LocalDate paymentDate = request.paymentDate() != null
                ? request.paymentDate()
                : payment.getPaymentDate();
        UUID paymentMethodId = request.paymentMethodId() != null
                ? request.paymentMethodId()
                : payment.getPaymentMethodId();
        String notes = request.notes() != null
                ? request.notes()
                : payment.getNotes();

        Payment updated = payment.withUpdatedDetails(referenceNumber,
                paymentDate, paymentMethodId, notes);
        Payment saved = paymentRepository.save(updated);
        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_RECEIVABLES",
                "EDIT", "PAYMENT", saved.getId().value(),
                saved.getPaymentNumber().value(), null));
        return PaymentMapper.toResponse(saved);
    }
}
