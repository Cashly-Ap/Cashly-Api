package com.cashly.cashly_api.sales.quotes.application.ports.out;

import java.util.UUID;

public interface QuoteConversionNotificationGateway {
    void convertedToOrder(UUID customerId, UUID quoteId, String orderNumber);
    void convertedToInvoice(UUID customerId, UUID quoteId,
            String invoiceNumber);
}
