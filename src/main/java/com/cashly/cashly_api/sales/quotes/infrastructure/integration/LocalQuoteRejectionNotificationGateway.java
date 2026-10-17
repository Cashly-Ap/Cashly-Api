package com.cashly.cashly_api.sales.quotes.infrastructure.integration;

import com.cashly.cashly_api.platform.notifications.application.ports.in.QuoteRejectionNotifications;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteRejectionNotificationGateway;
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
