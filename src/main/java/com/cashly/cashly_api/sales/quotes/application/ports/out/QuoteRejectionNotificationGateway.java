package com.cashly.cashly_api.sales.quotes.application.ports.out;

import java.util.UUID;

public interface QuoteRejectionNotificationGateway {
    void rejected(UUID customerId, UUID quoteId, String reason);
}
