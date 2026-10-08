package com.cashly.cashly_api.quotes.infrastructure.integration;

import com.cashly.cashly_api.notifications.application.api.QuoteConversionNotifications;
import com.cashly.cashly_api.quotes.application.contracts.QuoteConversionNotificationGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class QuoteConversionNotificationAdapter implements QuoteConversionNotificationGateway {
    private final QuoteConversionNotifications notifications;

    public QuoteConversionNotificationAdapter(QuoteConversionNotifications notifications) {
        this.notifications = notifications;
    }

    @Override
    public void convertedToOrder(UUID customerId, UUID quoteId, String orderNumber) {
        notifications.convertedToOrder(customerId, quoteId, orderNumber);
    }

    @Override
    public void convertedToInvoice(UUID customerId, UUID quoteId, String invoiceNumber) {
        notifications.convertedToInvoice(customerId, quoteId, invoiceNumber);
    }
}
