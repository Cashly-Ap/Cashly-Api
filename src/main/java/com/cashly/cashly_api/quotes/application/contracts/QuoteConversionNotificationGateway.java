package com.cashly.cashly_api.quotes.application.contracts;

import java.util.UUID;

public interface QuoteConversionNotificationGateway {
    void convertedToOrder(UUID customerId, UUID quoteId, String orderNumber);
    void convertedToInvoice(UUID customerId, UUID quoteId, String invoiceNumber);
}
