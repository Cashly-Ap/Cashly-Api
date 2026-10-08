package com.cashly.cashly_api.quotes.application.contracts;

import java.util.UUID;

public interface QuoteRejectionNotificationGateway {
    void rejected(UUID customerId, UUID quoteId, String reason);
}
