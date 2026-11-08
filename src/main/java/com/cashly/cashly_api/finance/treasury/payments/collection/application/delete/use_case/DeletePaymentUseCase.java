package com.cashly.cashly_api.finance.treasury.payments.collection.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class DeletePaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeletePaymentUseCase(PaymentRepository paymentRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.paymentRepository = paymentRepository;
    }

    @Audited(module = "finance", entityType = "PAYMENT", action = "DELETE")
    public void execute(UUID id) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + id));

        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_RECEIVABLES",
                "DELETE", "PAYMENT", id,
                id.toString(), null));
        paymentRepository.deleteById(PaymentId.of(id));
    }
}
