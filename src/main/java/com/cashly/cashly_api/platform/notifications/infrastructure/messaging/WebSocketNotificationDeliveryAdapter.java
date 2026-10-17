package com.cashly.cashly_api.platform.notifications.infrastructure.messaging;

import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationDeliveryGateway;
import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketNotificationDeliveryAdapter implements NotificationDeliveryGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNotificationDeliveryAdapter.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationDeliveryAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void deliver(Notification notification) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        send(notification);
                    } catch (RuntimeException exception) {
                        LOGGER.warn("WebSocket delivery failed for persisted notification {}", notification.getId(), exception);
                    }
                }
            });
        } else {
            send(notification);
        }
    }

    private void send(Notification notification) {
        String destination = notification.getUserId() != null
                ? "/topic/notifications/user/" + notification.getUserId()
                : "/topic/notifications/customer/" + notification.getCustomerId();
        messagingTemplate.convertAndSend(destination, notification);
    }
}
