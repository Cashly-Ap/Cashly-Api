package com.cashly.cashly_api.platform.notifications.application.quotes.services;

import com.cashly.cashly_api.platform.notifications.application.services.NotificationService;

import com.cashly.cashly_api.platform.notifications.application.ports.in.QuoteRejectionNotifications;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class QuoteRejectionNotificationService implements QuoteRejectionNotifications {
    private final NotificationService notifications;

    public QuoteRejectionNotificationService(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Override
    public void rejected(UUID customerId, UUID quoteId, String reason) {
        notifications.notifyCustomerQuoteRejected(customerId, quoteId, reason);
    }
}
