package com.cashly.cashly_api.notifications.application.api;

import java.util.UUID;

public interface QuoteConversionNotifications {
    void convertedToOrder(UUID customerId, UUID quoteId, String orderNumber);
    void convertedToInvoice(UUID customerId, UUID quoteId, String invoiceNumber);
}
