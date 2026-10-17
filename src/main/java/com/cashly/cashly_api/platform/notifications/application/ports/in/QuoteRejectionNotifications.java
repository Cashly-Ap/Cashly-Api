package com.cashly.cashly_api.platform.notifications.application.ports.in;

import java.util.UUID;

public interface QuoteRejectionNotifications {
    void rejected(UUID customerId, UUID quoteId, String reason);
}
