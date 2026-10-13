package com.cashly.cashly_api.notifications.application.ports.in;

import java.util.UUID;

public interface QuoteConversionNotifications {
    void convertedToOrder(UUID customerId, UUID quoteId, String orderNumber);
    void convertedToInvoice(UUID customerId, UUID quoteId, String invoiceNumber);
}
