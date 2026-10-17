package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.platform.notifications.application.services.NotificationService;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentNotificationGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalPaymentNotificationGateway implements PaymentNotificationGateway {
    private final NotificationService notifications;

    public LocalPaymentNotificationGateway(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Override
    public void approved(UUID customerId, UUID paymentId) {
        notifications.notifyCustomerPaymentApproved(customerId, paymentId);
    }

    @Override
    public void rejected(UUID customerId, UUID paymentId, String reason) {
        notifications.notifyCustomerPaymentRejected(customerId, paymentId, reason);
    }
}
