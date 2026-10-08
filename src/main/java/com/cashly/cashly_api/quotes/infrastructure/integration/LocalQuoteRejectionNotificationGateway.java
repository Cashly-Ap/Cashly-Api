package com.cashly.cashly_api.quotes.infrastructure.integration;

import com.cashly.cashly_api.notifications.application.api.QuoteRejectionNotifications;
import com.cashly.cashly_api.quotes.application.contracts.QuoteRejectionNotificationGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LocalQuoteRejectionNotificationGateway implements QuoteRejectionNotificationGateway {
    private final QuoteRejectionNotifications notifications;

    public LocalQuoteRejectionNotificationGateway(QuoteRejectionNotifications notifications) {
        this.notifications = notifications;
    }

    @Override
    public void rejected(UUID customerId, UUID quoteId, String reason) {
        notifications.rejected(customerId, quoteId, reason);
    }
}
