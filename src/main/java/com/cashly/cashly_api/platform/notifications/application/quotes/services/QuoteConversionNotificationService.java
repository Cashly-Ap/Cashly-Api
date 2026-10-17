package com.cashly.cashly_api.platform.notifications.application.quotes.services;

import com.cashly.cashly_api.platform.notifications.application.services.NotificationService;

import com.cashly.cashly_api.platform.notifications.application.ports.in.QuoteConversionNotifications;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class QuoteConversionNotificationService implements QuoteConversionNotifications {
    private final NotificationService notifications;

    public QuoteConversionNotificationService(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Override
    public void convertedToOrder(UUID customerId, UUID quoteId, String orderNumber) {
        notifications.notifyCustomerQuoteConvertedToOrder(customerId, quoteId, orderNumber);
    }

    @Override
    public void convertedToInvoice(UUID customerId, UUID quoteId, String invoiceNumber) {
        notifications.notifyCustomerQuoteConvertedToInvoice(customerId, quoteId, invoiceNumber);
    }
}
