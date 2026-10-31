package com.cashly.cashly_api.platform.notifications.infrastructure.messaging;

import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationDeliveryGateway;
import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
@Component
public class WebSocketNotificationDeliveryAdapter
        implements
            NotificationDeliveryGateway {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(WebSocketNotificationDeliveryAdapter.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository preferences;
    private final NotificationTranslator translator = new NotificationTranslator();

    public WebSocketNotificationDeliveryAdapter(
            SimpMessagingTemplate messagingTemplate,
            com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository preferences) {
        this.preferences = preferences;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void deliver(Notification notification) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager
                    .registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                send(notification);
                            } catch (RuntimeException exception) {
                                LOGGER.warn(
                                        "WebSocket delivery failed for persisted notification {}",
                                        notification.getId(), exception);
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
                : "/topic/notifications/customer/"
                        + notification.getCustomerId();
        String language = notification.getUserId() == null
                ? "es"
                : preferences.languageFor(notification.getUserId());
        messagingTemplate.convertAndSend(destination,
                translator.render(notification, java.util.Locale
                        .forLanguageTag("en".equals(language) ? "en" : "es")));
    }
}
