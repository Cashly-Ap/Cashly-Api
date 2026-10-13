package com.cashly.cashly_api.orders.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalOrderNotificationAdapter implements OrderNotificationGateway {
    private final NotificationService notifications;

    public LocalOrderNotificationAdapter(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Override
    public void notifyCustomerOrderApproved(UUID customerId, UUID orderId) {
        notifications.notifyCustomerOrderApproved(customerId, orderId);
    }

    @Override
    public void notifyCustomerOrderRejected(UUID customerId, UUID orderId, String reason) {
        notifications.notifyCustomerOrderRejected(customerId, orderId, reason);
    }

    @Override
    public void notifyCustomerOrderConvertedToInvoice(UUID customerId, UUID orderId, String invoiceNumber) {
        notifications.notifyCustomerOrderConvertedToInvoice(customerId, orderId, invoiceNumber);
    }
}
