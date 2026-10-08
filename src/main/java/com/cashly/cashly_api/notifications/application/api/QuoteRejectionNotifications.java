package com.cashly.cashly_api.notifications.application.api;

import java.util.UUID;

public interface QuoteRejectionNotifications {
    void rejected(UUID customerId, UUID quoteId, String reason);
}
